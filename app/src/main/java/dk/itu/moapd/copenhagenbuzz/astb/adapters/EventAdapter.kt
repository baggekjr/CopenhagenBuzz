package dk.itu.moapd.copenhagenbuzz.astb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.ImageView
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.EventRowItemBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class EventAdapter(private val context: Context, private val events: List<Event>) :
    ArrayAdapter<Event>(context, R.layout.event_row_item, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.event_row_item, parent, false)

        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)

        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event)
        }

        view.tag = viewHolder

        return view
    }

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event) {
        with(viewHolder) {
            eventIcon.setImageResource(R.drawable.baseline_map_24)
            eventName.text = event.eventName
            eventLocation.text = event.eventLocation
            eventDate.text = event.startDate.toString()
            eventType.text = event.eventType
            eventDescription.text = event.eventDescription
        }
    }

    private class ViewHolder(view: View) {
        val eventIcon = view.findViewById<ImageView>(R.id.event_icon)
        val eventName = view.findViewById<TextView>(R.id.event_name)
        val eventLocation = view.findViewById<TextView>(R.id.event_location)
        val eventDate = view.findViewById<TextView>(R.id.event_date)
        val eventType = view.findViewById<TextView>(R.id.event_type)
        val eventDescription = view.findViewById<TextView>(R.id.event_description)
    }
}