package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.adapters.FavoriteAdapter
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val viewModel: DataViewModel by viewModels()



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

        with (binding){
            viewModel.favorites.observe(viewLifecycleOwner) {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())

                val adapter = FavoriteAdapter(it)
                recyclerView.adapter = adapter
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}