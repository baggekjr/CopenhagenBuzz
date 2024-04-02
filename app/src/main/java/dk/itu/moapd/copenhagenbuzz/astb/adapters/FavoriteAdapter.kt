package dk.itu.moapd.copenhagenbuzz.astb.adapters

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FavoritesRowItemBinding
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class FavoriteAdapter(private val favorites: List<Event>) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    class ViewHolder(private val binding: FavoritesRowItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(event: Event) {
            with(binding) {
                eventIcon.setImageResource(R.drawable.baseline_map_24)
                eventName.text = event.eventName
                eventType.text = event.eventType

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = FavoritesRowItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
        .let(::ViewHolder)



    override fun onBindViewHolder(holder: FavoriteAdapter.ViewHolder, position: Int) {
        Log.d(TAG, "Populate at position: $position")
        favorites[position].let(holder::bind)


    }

    override fun getItemCount() = favorites.size



}