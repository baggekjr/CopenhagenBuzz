package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.adapters.FavoriteAdapter
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val viewModel: DataViewModel by activityViewModels()

    private val EVENTS = "events"
    private val BUZZ = "CopenhagenBuzz"
    private val FAVORITES = "favorites"


    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentFavoritesBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())



        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            val query = Firebase.database(DATABASE_URL).reference
                .child(BUZZ)
                .child(FAVORITES)
                .child(userId)



            val databaseReference = Firebase.database(DATABASE_URL).reference
                .child(BUZZ)
                .child(EVENTS)


            val options = FirebaseRecyclerOptions.Builder<Event>()
                .setIndexedQuery(query, databaseReference, Event::class.java)
                .setLifecycleOwner(this)
                .build()


            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())

            }
            val adapter = FavoriteAdapter(options)


            with(binding) {
                viewModel.favorites.observe(viewLifecycleOwner) {}
                recyclerView.adapter = adapter
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}