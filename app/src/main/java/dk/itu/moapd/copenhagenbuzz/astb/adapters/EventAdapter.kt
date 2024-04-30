package dk.itu.moapd.copenhagenbuzz.astb.adapters

import DeleteEventDialogFragment
import android.content.ContentValues.TAG
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.fragments.UpdateEventDialogFragment
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import java.util.Locale

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
            //eventIcon.setImageResource(R.drawable.baseline_map_24)
            eventName.text = event.eventName
            eventLocation.text = event.eventLocation?.address
            eventDate.text = event.startDate.toString()
            eventType.text = event.eventType
            eventDescription.text = event.eventDescription
            val j = event.eventIcon
            Log.e(TAG, "Måske: $j")

            loadImageToView(event, eventIcon)

            val currentUser = auth.currentUser
            val currentUserUid = currentUser?.uid
            val eventUserId = event.userId

            if(currentUserUid == eventUserId) {
                editButton?.visibility = View.VISIBLE
                editButton?.setOnClickListener {
                    UpdateEventDialogFragment(event, position, this@EventAdapter).apply {
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


    private fun loadImageToView(event: Event, eventImage: ImageView) {
        val placeholderImage = R.drawable.baseline_celebration_24
        val i = event.eventIcon
        Log.e(TAG,"{omg: $i}")
        event.eventIcon?.let { imgUrl ->
            val storageReference = Firebase.storage.reference
            val imageRef = storageReference.child("your_image_directory/$imgUrl") // Replace "your_image_directory" with the actual directory path in your Firebase Storage

            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                Log.e(TAG, "Image URL for $imgUrl: $imageUrl") // Logging the image URL

                // Load the image using Picasso or any other image loading library
                Picasso.get()
                    .load(uri)
                    .placeholder(placeholderImage).error(placeholderImage)
                    .into(eventImage)
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting download URL for $imgUrl: $exception")
                // Load placeholder image or handle error
                Picasso.get().load(placeholderImage).into(eventImage)
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


