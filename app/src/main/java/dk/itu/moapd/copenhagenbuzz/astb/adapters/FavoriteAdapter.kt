package dk.itu.moapd.copenhagenbuzz.astb.adapters

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FavoritesRowItemBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event


class FavoriteAdapter(options: FirebaseRecyclerOptions<Event>) : FirebaseRecyclerAdapter<Event, FavoriteAdapter.ViewHolder>(options) {

    inner class ViewHolder(private val binding: FavoritesRowItemBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun bind(favorite: Event) {
            with(binding) {
                eventName.text = favorite.eventName
                eventType.text = favorite.eventType
                loadImageToView(favorite, eventIcon)

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

}