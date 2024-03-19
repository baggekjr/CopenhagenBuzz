package dk.itu.moapd.copenhagenbuzz.astb.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class FavoriteAdapter(private val favorites: List<Event>) : RecyclerView.Adapter<FavoriteAdapter.Viewholder>() {

    class Viewholder(private val binding: FragmentFavoritesBinding) : RecyclerView.ViewHolder(binding.root)
    {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteAdapter.Viewholder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: FavoriteAdapter.Viewholder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount() = favorites.size


}