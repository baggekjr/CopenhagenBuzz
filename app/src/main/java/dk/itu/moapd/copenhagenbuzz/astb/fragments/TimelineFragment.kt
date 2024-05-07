package dk.itu.moapd.copenhagenbuzz.astb.fragments

import DeleteEventDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.astb.interfaces.OnFavoriteClickListener
import dk.itu.moapd.copenhagenbuzz.astb.interfaces.OnItemClickListener
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [TimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimelineFragment : Fragment(), OnFavoriteClickListener, OnItemClickListener {

    private var _binding: FragmentTimelineBinding? = null
    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var eventAdapter: EventAdapter

    private val EVENTS = "events"
    private val BUZZ = "CopenhagenBuzz"


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
        loadEvents()

        setupSearch()

    }

    /**
     * Loading standard timeline with all events sorted by startdate
     */
    private fun loadEvents() {
        val query = Firebase.database(DATABASE_URL).reference
            .child(BUZZ)
            .child(EVENTS)
            .orderByChild("startDate")

        val options = FirebaseListOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .setLayout(R.layout.event_row_item)
            .setLifecycleOwner(this)
            .build()

        eventAdapter =
            EventAdapter(requireContext(), options, this, this)

        binding.listView.adapter = eventAdapter
    }


    /**
     * Creates searchview where user is able search for event via name. Has to be searched with
     * correct capital letters in order of appearance. The search query is then sent to performSearch()
     * if it is not null or blank. Otherwise it will load all events.
     */
    private fun setupSearch() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_bar, menu)

                val searchItem = menu.findItem(R.id.search_bar)
                val searchView = searchItem?.actionView as SearchView

                searchView.queryHint = getString(R.string.search)
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        if (!query.isNullOrBlank()) {
                            performSearch(query)
                        } else {
                            loadEvents()
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        if (newText.isNullOrBlank()) {
                            loadEvents()
                        }
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.search_bar -> {
                        // Expand or collapse the search view
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Function that performs the search and creates a new listView with the results of the search.
     *
     * @param query is the String that the user has put in as the search query aka the eventName.
     */

    private fun performSearch(query: String) {
        val db = Firebase.database(DATABASE_URL).reference.child(BUZZ)
        val search = db
            .child(EVENTS)
            .orderByChild("eventName")
            .startAt(query)
            .endAt(query + "\uf8ff")

        val searchOption = FirebaseListOptions.Builder<Event>()
            .setQuery(search, Event::class.java)
            .setLayout(R.layout.event_row_item)
            .setLifecycleOwner(this)
            .build()

        val adapter = EventAdapter(
            requireContext(),
            searchOption,
            this, this
        )
        binding.listView.adapter = adapter

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Implements the interface methods
     *
     * @param ref is the reference to the event that is either added to favorites or removed from favorites
     * @param isChecked Boolean to inform if the favoriteCheckbox is already checked or not
     */
    override fun onFavoriteClick(ref: DatabaseReference, isChecked: Boolean) {
        dataViewModel.updateFavorite(ref, isChecked)
    }

    override fun isFavorite(eventId: String, onResult: (isFavorite: Boolean) -> Unit) {
        return dataViewModel.isFavorite(eventId, onResult)
    }

    override fun onDeleteEvent(event: Event, position: Int) {
        binding.listView.adapter.let {
            DeleteEventDialogFragment(event, position, it as EventAdapter).apply {
                isCancelable = false
            }.show(parentFragmentManager, "DeleteEventDialogFragment")
        }


    }

    override fun onEditEvent(event: Event, position: Int) {
        binding.listView.adapter.let {
            UpdateEventDialogFragment(event, position, it as EventAdapter, requireView()).apply {
                isCancelable = false
            }.show(parentFragmentManager, "UpdateEventFragment")
        }    }
}


