package dk.itu.moapd.copenhagenbuzz.astb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.Utils.DateFormatter
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.interfaces.OnItemClickListener
import dk.itu.moapd.copenhagenbuzz.astb.interfaces.OnFavoriteClickListener
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class EventAdapter(private val fragmentManager: FragmentManager, private val context: Context, private val options: FirebaseListOptions<Event>, private val  onFavoriteClickListener: OnFavoriteClickListener, private val dialogsOnClickListener: OnItemClickListener) :
    FirebaseListAdapter<Event>(options) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.event_row_item, parent, false)

        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)

        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event, position)
        }

        view.tag = viewHolder

        return view
    }


    override fun populateView(v: View, model: Event, position: Int) {
        val viewHolder = ViewHolder(v)
        populateViewHolder(viewHolder, model, position)


    }

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event, position: Int) {
        with(viewHolder) {
            bindEvent(viewHolder, event)

            val currentUser = auth.currentUser
            val currentUserUid = currentUser?.uid
            val eventUserId = event.userId

            if (currentUser != null) {
                favoriteCheckbox.visibility = View.VISIBLE
                bindFavorites(viewHolder, position)
            } else {
                favoriteCheckbox.visibility = View.GONE
            }

            if(currentUserUid == eventUserId) {
                editButton?.visibility = View.VISIBLE
                editButton?.setOnClickListener {
                    dialogsOnClickListener.onEditEvent(event, position)
                }

                deleteButton?.visibility = View.VISIBLE
                deleteButton?.setOnClickListener {
                    dialogsOnClickListener.onDeleteEvent(event, position)
                }

            } else {
                editButton?.visibility = View.GONE
                deleteButton?.visibility = View.GONE
            }
        }
    }
    private fun bindEvent(viewHolder: ViewHolder, event: Event) {
        with(viewHolder){
            //eventIcon.setImageResource(R.drawable.baseline_map_24)
            eventName.text = event.eventName
            eventLocation.text = event.eventLocation?.address

            // Set the startDate and endDate to TextView objects
            val startDateText =
                event.startDate?.let { DateFormatter.formatDate(it) } ?: "Start Date Unavailable"
            val endDateText =
                event.endDate?.let { DateFormatter.formatDate(it) } ?: "End Date Unavailable"
            startDate.text = startDateText
            endDate.text = " - $endDateText"

            eventType.text = event.eventType
            eventDescription.text = event.eventDescription
            loadImageToView(event, eventIcon)
        }
    }

    private fun bindFavorites(viewHolder: ViewHolder, position: Int){

        auth.currentUser?.uid?.let { userId ->
            with(viewHolder) {
                val ref = getId(position)
                ref.key?.let {
                    onFavoriteClickListener.isFavorite(it) { exists ->
                        favoriteCheckbox.isChecked = exists
                    }
                }

                favoriteCheckbox.setOnCheckedChangeListener { _, isChecked ->

                    onFavoriteClickListener.onFavoriteClick(ref ,isChecked)

                }


            }
        }
    }



    private class ViewHolder(view: View) {
        val favoriteCheckbox= view.findViewById<CheckBox>(R.id.favorite_button)
        val eventIcon= view.findViewById<ImageView>(R.id.event_icon)
        val eventName= view.findViewById<TextView>(R.id.event_name)
        val eventLocation= view.findViewById<TextView>(R.id.event_location)
        val startDate = view.findViewById<TextView>(R.id.start_date)
        val endDate = view.findViewById<TextView>(R.id.end_date)
        val eventType = view.findViewById<TextView>(R.id.event_type)
        val eventDescription = view.findViewById<TextView>(R.id.event_description)
        val editButton = view.findViewById<Button>(R.id.edit_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_button)
    }

    private fun loadImageToView(event: Event, eventImage: ImageView) {
        val placeholderImage = R.drawable.baseline_find_replace_24

        event.eventIcon?.let { imgUrl ->
            com.google.firebase.Firebase.storage(BUCKET_URL).reference
                .child(imgUrl).downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .placeholder(placeholderImage)
                        .error(placeholderImage)
                        .into(eventImage)
                }
        }
    }
    fun getId(position: Int): DatabaseReference{
        return getRef(position)
    }
}


