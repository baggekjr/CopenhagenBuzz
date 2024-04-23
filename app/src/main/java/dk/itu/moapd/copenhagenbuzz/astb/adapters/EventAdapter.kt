package dk.itu.moapd.copenhagenbuzz.astb.adapters

import DeleteEventDialogFragment
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.fragments.UpdateEventDialogFragment
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class EventAdapter(private val fragmentManager: FragmentManager, private val context: Context, private val options: FirebaseListOptions<Event>) :
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


        // Set OnClickListener for the edit button

        return view
    }


    override fun populateView(v: View, model: Event, position: Int) {
        val viewHolder = ViewHolder(v)
        populateViewHolder(viewHolder, model, position)


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




    private class ViewHolder(view: View) {
        val eventIcon = view.findViewById<ImageView>(R.id.event_icon)
        val eventName = view.findViewById<TextView>(R.id.event_name)
        val eventLocation = view.findViewById<TextView>(R.id.event_location)
        val eventDate = view.findViewById<TextView>(R.id.event_date)
        val eventType = view.findViewById<TextView>(R.id.event_type)
        val eventDescription = view.findViewById<TextView>(R.id.event_description)
        val editButton = view.findViewById<Button>(R.id.edit_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_button)


    }

    private fun getId(position: Int): DatabaseReference{
        return getRef(position)
    }
}