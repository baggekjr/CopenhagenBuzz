package dk.itu.moapd.copenhagenbuzz.astb.adapters

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FavoritesRowItemBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.Favorite

class FavoriteAdapter(options: FirebaseRecyclerOptions<Event>) : FirebaseRecyclerAdapter<Event, FavoriteAdapter.ViewHolder>(options) {

    inner class ViewHolder(private val binding: FavoritesRowItemBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun bind(favorite: Event) {
            with(binding) {
                eventName.text = favorite.eventName
                eventType.text = favorite.eventType

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = FavoritesRowItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
        .let(::ViewHolder)

    override fun onBindViewHolder(holder: FavoriteAdapter.ViewHolder, position: Int, model: Event) {
        Log.d(TAG, "Populate at position: $position")
        model.let(holder::bind)
    }
}