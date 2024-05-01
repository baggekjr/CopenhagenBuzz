package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.interfaces.OnFavoriteClickListener
import dk.itu.moapd.copenhagenbuzz.astb.models.Event


/**
 * A simple [Fragment] subclass.
 * Use the [TimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimelineFragment : Fragment(), OnFavoriteClickListener {

    private var _binding: FragmentTimelineBinding? = null
    private val dataViewModel: DataViewModel by activityViewModels()
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

            val query = Firebase.database(DATABASE_URL).reference
                .child("CopenhagenBuzz")
                .child("events")
                .orderByChild("startDate")

            val options = FirebaseListOptions.Builder<Event>()
                .setQuery(query, Event::class.java)
                .setLayout(R.layout.event_row_item)
                .setLifecycleOwner(this)
                .build()

            eventAdapter = EventAdapter(requireActivity().supportFragmentManager, requireContext(), options, this)

            binding.listView.adapter = eventAdapter
            // Set up data binding and lifecycle owner.


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFavoriteClick(ref: DatabaseReference, event: Event, isChecked: Boolean) {
        dataViewModel.updateFavorite(ref, isChecked)
    }

    override fun isFavorite(eventId: String, onResult: (isFavorite: Boolean) -> Unit){
        return dataViewModel.isFavorite(eventId, onResult)
    }


}