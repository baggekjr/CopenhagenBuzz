package dk.itu.moapd.copenhagenbuzz.astb.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.material.button.MaterialButton
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class EventAdapter(activity: Activity, private val context: Context, private val options: FirebaseListOptions<Event>, private val onFavoriteCheckedChanged: (Event, Boolean) -> Unit) :
    FirebaseListAdapter<Event>(options) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.event_row_item, parent, false)

        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)


        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event)
            viewHolder.favoriteCheckbox.setOnCheckedChangeListener {_, isChecked ->
                onFavoriteCheckedChanged(event, isChecked)
            }
        }

        view.tag = viewHolder

        return view
    }

    override fun populateView(v: View, model: Event, position: Int) {
        val viewHolder = ViewHolder(v)
        populateViewHolder(viewHolder, model)
        viewHolder.favoriteCheckbox.setOnCheckedChangeListener {event, isChecked ->
            onFavoriteCheckedChanged(model, isChecked)
        }

    }

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event) {
        with(viewHolder) {
            eventIcon.setImageResource(R.drawable.baseline_map_24)
            eventName.text = event.eventName
            eventLocation.text = event.eventLocation
            eventDate.text = event.startDate.toString()
            eventType.text = event.eventType
            eventDescription.text = event.eventDescription
            if (event.isFavorite) {
                favoriteCheckbox.setButtonDrawable(R.drawable.baseline_favorite_24)
            } else {
                favoriteCheckbox.setButtonDrawable(R.drawable.baseline_favorite_border_24)
            }
        }
    }

    private class ViewHolder(view: View) {
        val favoriteCheckbox = view.findViewById<CheckBox>(R.id.favorite_button)
        //val favoriteButton = view.findViewById<MaterialButton>(R.id.favorite_button)
        val eventIcon = view.findViewById<ImageView>(R.id.event_icon)
        val eventName = view.findViewById<TextView>(R.id.event_name)
        val eventLocation = view.findViewById<TextView>(R.id.event_location)
        val eventDate = view.findViewById<TextView>(R.id.event_date)
        val eventType = view.findViewById<TextView>(R.id.event_type)
        val eventDescription = view.findViewById<TextView>(R.id.event_description)


    }
}