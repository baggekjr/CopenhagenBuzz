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
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.activities.MainActivity
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import java.util.Date


/**
 * A simple [Fragment] subclass.
 * Use the [EventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventFragment : Fragment() {
    private var _binding: FragmentEventBinding? = null

    // A set of private constants used in this class .
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    // An instance of the ‘Event ‘ class.
    private val event: Event = Event( "","", "", "", "", "")

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentEventBinding.inflate(inflater, container, false).also {
        _binding = it

        // Listener for user interaction in the ‘Add Event‘ button.
        binding.addEventButton.setOnClickListener {
            // Only execute the following code when the user fills all ‘EditText ‘.
            handleEventButtonOnClick()
        }

        // Listener for user interaction in the "Add event date" textfield
        binding.editTextEventDate.setOnClickListener {
            handleDateOnClick()
        }
    }.root


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        // TODO: Fix it so you dont have to press twice for it to open.
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").build()
        dateRangePicker.show(parentFragmentManager, "DatePicker")

        dateRangePicker.addOnPositiveButtonClickListener { datePicked ->
            val startDate = datePicked.first
            val endDate = datePicked.second

            binding.editTextEventDate.setText(
                convertLongToDate(startDate) + " - " + convertLongToDate(endDate)
            )
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
            val eventIcon = "picture"
            val eventName = binding.editTextEventName.text.toString().trim()
            val eventLocation = binding.editTextEventLocation.text.toString().trim()
            val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            Event(eventIcon, eventName, eventLocation, eventDate, eventType, eventDescription)

            // Show snackbar with the event
            Snackbar.make(binding.root, eventName+" " +eventLocation+" "+eventDate+" "+eventType+" "+eventDescription, Snackbar.LENGTH_SHORT).show()


            // Write in the 'Logcat' system
            showMessage()
        }
    }


    private fun showMessage() {
        Log.d(EventFragment.TAG, event.toString())
    }
}