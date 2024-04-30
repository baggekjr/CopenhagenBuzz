package dk.itu.moapd.copenhagenbuzz.astb.adapters

import DeleteEventDialogFragment
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.fragments.UpdateEventDialogFragment
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class EventAdapter(private val fragmentManager: FragmentManager, private val context: Context, private val options: FirebaseListOptions<Event>) :
    FirebaseListAdapter<Event>(options) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()



    //private val favoritedEvents = mutableListOf<Event>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.event_row_item, parent, false)

        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)

        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event, position)

        }

        /*
        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event, position)
            viewHolder.favoriteCheckbox.setOnCheckedChangeListener {_, isChecked ->
                onFavoriteCheckedChanged(event, isChecked)
                if(isChecked) {
                    event.favoritedBy?.add(user.uid)
                } else {
                    event.favoritedBy?.remove(user.uid)
                }
            }
        }

         */

        view.tag = viewHolder

        return view
    }

    override fun populateView(v: View, model: Event, position: Int) {
        val viewHolder = ViewHolder(v)
        populateViewHolder(viewHolder, model, position)

        setFavoriteListener(viewHolder, model, position)

    }

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event, position: Int) {
        with(viewHolder) {

            eventIcon.setImageResource(R.drawable.baseline_map_24)
            eventName.text = event.eventName
            eventLocation.text = event.eventLocation
            eventDate.text = event.startDate.toString()
            eventType.text = event.eventType
            eventDescription.text = event.eventDescription

            val currentUser = auth.currentUser
            val currentUserUid = currentUser?.uid

            if (currentUser != null) {
                favoriteCheckbox.visibility = View.VISIBLE
                favoriteCheckbox.isChecked= event.favoritedBy?.contains(currentUserUid) ?: false
            } else {
                favoriteCheckbox.visibility = View.GONE
            }


            val eventUserId = event.userId

            if(currentUserUid == eventUserId) {
                editButton?.visibility = View.VISIBLE
                editButton?.setOnClickListener {
                    UpdateEventDialogFragment(event, position, getId(position)).apply {
                        isCancelable = false
                    }.show(fragmentManager, "UpdateEventFragment")
                }

                deleteButton?.visibility = View.VISIBLE
                deleteButton?.setOnClickListener { adapter ->
                    DeleteEventDialogFragment(event, position, this@EventAdapter).apply {
                        isCancelable = false
                    }.show(fragmentManager, "DeleteEventDialogFragment")
                }
            } else {
                editButton?.visibility = View.GONE
                deleteButton?.visibility = View.GONE
            }
        }
    }


    private fun setFavoriteListener(viewHolder: ViewHolder, event: Event, position: Int) {

        val currentUserUid = auth.currentUser?.uid ?: return
            viewHolder.favoriteCheckbox.setOnCheckedChangeListener{ _ , isChecked ->

                val newFavoritedBy = event.favoritedBy?.toMutableList() ?: mutableListOf()

                if (isChecked) {
                    if (!newFavoritedBy.contains(currentUserUid)) {
                        newFavoritedBy.add(currentUserUid)
                    }
                } else {
                    newFavoritedBy.remove(currentUserUid)

                }


                val updatedEvent = Event (
                    userId = event.userId,
                    eventName = event.eventName,
                    eventLocation = event.eventLocation,
                    eventType = event.eventType,
                    eventDescription = event.eventDescription,
                    eventIcon = event.eventIcon,
                    startDate = event.startDate,
                    favoritedBy = newFavoritedBy
                )

                val ref = getRef(position)
                try {
                    updateEventFavorite(ref, updatedEvent)
                    updateFavoriteList(ref, updatedEvent, isChecked)


                    println("Save successfull")

                } catch (exception : Exception){
                    println("Database save failure with following exception: $exception")


                }

            }

    }

    private fun updateEventFavorite(ref: DatabaseReference, updatedEvent: Event) {
        ref.setValue(updatedEvent)

    }

    private fun updateFavoriteList(ref: DatabaseReference, updatedEvent: Event, isFavorited: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser
        val db = Firebase.database(DATABASE_URL).reference.child("CopenhagenBuzz")

        db.child("favorites").child(user!!.uid).get()
            .addOnSuccessListener {
                val eventId = ref.key

                val currentEvent = db.child("favorites")
                    .child(user.uid).child(eventId!!)

                if (isFavorited) {
                    currentEvent
                        .setValue(true)
                        .addOnFailureListener {
                            throw it
                        }
                } else {
                    currentEvent
                        .removeValue()
                        .addOnFailureListener {
                            throw it
                        }
                }
            }.addOnFailureListener {
                throw it
            }

    }


    private fun getId(position: Int): DatabaseReference{
        return getRef(position)
    }

    private class ViewHolder(view: View) {
        val favoriteCheckbox= view.findViewById<CheckBox>(R.id.favorite_button)
        val eventIcon= view.findViewById<ImageView>(R.id.event_icon)
        val eventName= view.findViewById<TextView>(R.id.event_name)
        val eventLocation= view.findViewById<TextView>(R.id.event_location)
        val eventDate = view.findViewById<TextView>(R.id.event_date)
        val eventType = view.findViewById<TextView>(R.id.event_type)
        val eventDescription= view.findViewById<TextView>(R.id.event_description)
        val editButton= view.findViewById<Button>(R.id.edit_button)
        val deleteButton= view.findViewById<Button>(R.id.delete_button)


    }
}