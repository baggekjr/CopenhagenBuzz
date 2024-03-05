package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.EventAdapter




/**
 * A simple [Fragment] subclass.
 * Use the [TimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val dataViewModel: DataViewModel by viewModels()
    private lateinit var eventAdapter: EventAdapter

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set up data binding and lifecycle owner.
        binding.apply {
            dataViewModel.events.observe(viewLifecycleOwner) {
                eventAdapter = EventAdapter(requireContext(), it)
                listView.adapter = eventAdapter
            }


        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}