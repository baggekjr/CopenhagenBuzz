package dk.itu.moapd.copenhagenbuzz.astb.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import androidx.lifecycle.LiveData
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.EventRowItemBinding

class EventAdapter(private val context: Context, private val events: List<Event>) :
    ArrayAdapter<Event>(context, R.layout.event_row_item, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.event_row_item, parent, false)

        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(EventRowItemBinding.bind(view))

        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event)
        }

        view.tag = viewHolder

        return view
    }

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event) {
        with(viewHolder) {
            binding.eventIcon.setImageResource(R.drawable.baseline_map_24)
            binding.eventName.text = event.eventName
            binding.eventLocation.text = event.eventLocation
            binding.eventDate.text = event.startDate
            binding.eventType.text = event.eventType
            binding.eventDescription.text = event.eventDescription
        }
    }

    private class ViewHolder(val binding: EventRowItemBinding)
}