package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL


/**
 * A simple [Fragment] subclass.
 * Use the [EventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventFragment : Fragment() {
    private var _binding: FragmentEventBinding? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference


    // A set of private constants used in this class .
    companion object {
        private val TAG = EventFragment::class.qualifiedName
    }

    // An instance of the ‘Event ‘ class.
    private val event: Event = Event( "","", "", "", 0, "", "")

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentEventBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database(DATABASE_URL).reference.child("CopenhagenBuzz")

        // Set up data binding and lifecycle owner.
        binding.apply {
            // Listener for user interaction in the ‘Add Event‘ button.
            addEventButton.setOnClickListener {
                // Only execute the following code when the user fills all ‘EditText‘.
                handleEventButtonOnClick()

            }

            // Listener for user interaction in the "Add event date" textfield
            editTextEventDate.setOnClickListener {
                handleDateOnClick()
            }

            setupDatePicker()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDatePicker() {
        with(binding.editTextEventDate) {
            keyListener = null
            setOnFocusChangeListener { _, infocus ->
                if (infocus) handleDateOnClick()
            }
        }
    }

    /**
     * Handles the date picker to pop up and the dates selected to get written out
     *
     * @param dateRangePicker The pop up calendar.
     * @param startDate The start date the user chose.
     * @param endDate The end date the user chose.
     *
     */

    private fun handleDateOnClick() {
        with(binding.editTextEventDate){
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").build()
            dateRangePicker.show(parentFragmentManager, "DatePicker")

            dateRangePicker.addOnPositiveButtonClickListener { datePicked ->
                val startDate = Date(datePicked.first)
                val endDate = Date(datePicked.second)

                //set event date

                val formatDate = SimpleDateFormat("E, MMM dd yyyy", Locale.US)
                val date = datePicked.first + datePicked.second

                setText(
                    // Don't do this - use a string resource
                    date.toString()
                    //formatDate.format(startDate) + " - " + formatDate.format(endDate)
                )
            }
        }
    }



    /**
     * Inspired from: Date Range Picker Android Material Design (Kotlin)????
     * Converts numbers in long format to dates
     *
     * @param date The date
     * @param format The formatted date
     *
     * @return The date in the correct format
     */
    private fun convertLongToDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat(
            "dd-MM-yyyy"
        )
        return format.format(date)
    }

    /**
     * Handle when the eventButton gets clicked.
     *
     * checks whether the textfields are empty or not. If they are all not empty it updates the
     * Event()
     *
     */
    private fun handleEventButtonOnClick() {
        if (binding.editTextEventName.text.toString()
                .isNotEmpty() && binding.editTextEventLocation.text.toString()
                .isNotEmpty() && binding.editTextEventDate.text.toString()
                .isNotEmpty() && binding.editTextEventType.text.toString()
                .isNotEmpty() && binding.editTextEventDescription.text.toString()
                .isNotEmpty()
        ) {
            // Update the object attributes.
            val userId = auth.currentUser!!.uid
            val eventIcon = "picture"
            val eventName = binding.editTextEventName.text.toString().trim()
            val eventLocation = binding.editTextEventLocation.text.toString().trim()
            val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            val newEvent = Event(userId, eventIcon, eventName, eventLocation, eventDate.toLong(), eventType, eventDescription)


            userId.let { uid ->
                database.child("events")
                    .child(uid)
                    .push()
                    .key?.let { event ->
                        database.child("events")
                            .child(event)
                            .setValue(newEvent).addOnSuccessListener { showMessage() }

                    }

            }


            // Show snackbar with the event
            Snackbar.make(
                requireView(),
                "$eventName $eventLocation $eventDate $eventType $eventDescription",
                Snackbar.LENGTH_SHORT
            ).show()



            // Write in the 'Logcat' system
            //showMessage()
        }
    }



    private fun showMessage() {
        Log.d(EventFragment.TAG, event.toString())

    }




}