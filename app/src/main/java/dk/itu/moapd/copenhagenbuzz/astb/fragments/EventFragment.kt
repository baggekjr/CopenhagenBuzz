package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.icu.text.SimpleDateFormat
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.GeocodingHelper
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.models.EventLocation
import io.github.cdimascio.dotenv.dotenv
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley


/**
 * A simple [Fragment] subclass.
 * Use the [EventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventFragment : Fragment() {
    private var _binding: FragmentEventBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var geocodingHelper: GeocodingHelper
    private lateinit var eventLocation: EventLocation

    // A set of private constants used in this class .
    companion object {
        private val TAG = EventFragment::class.qualifiedName
    }

    // An instance of the ‘Event ‘ class.
    private val event: Event = Event("", "", "", null, 0, "", "")

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
        geocodingHelper = GeocodingHelper(requireContext())


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
        with(binding.editTextEventDate) {
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

    private fun formatAddress(address: String) : String{
        val list = address
            .split(", ")

        // house number, street name, city name
        return "${list[1]} ${list[0]}, ${list[list.size - 6]}"
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
                .isNotEmpty()){


            val userId = auth.currentUser?.uid
            val eventName = binding.editTextEventName.text.toString().trim()
            val eventLocationStr = binding.editTextEventLocation.text.toString()
                .replace(' ', '+')
            val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            // Geocode the event location
            val key: String = "6630a5d972d20365148401gdsd0bcd5"

            val url = "https://geocode.maps.co/search?q=${eventLocationStr}+Copenhagen&api_key=${key}"

            val queue = Volley.newRequestQueue(activity?.applicationContext)


            val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
                response.toString()

                val data = response.getJSONObject(0)
                val lat = data.getDouble("lat")
                val lon = data.getDouble("lon")
                val prettyAddress = formatAddress(data.getString("display_name"))


                eventLocation = EventLocation(lat, lon, prettyAddress)

                // Save the event
                saveEvent(
                    userId!!,
                    eventName,
                    eventLocation,
                    eventDate,
                    eventType,
                    eventDescription
                )
            }, {error ->
                handleFailureVolley(error)
            })
            queue.add(request)
        } else {
            Snackbar.make(
                requireView(),
                "Please fill out all fields",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleFailureVolley(error: VolleyError?) {
        Snackbar.make(
            requireView(),
            "VolleyError",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun saveEvent(
        userId: String,
        eventName: String,
        eventLocation: EventLocation,
        eventDate: String,
        eventType: String,
        eventDescription: String
    ) {
        val eventIcon = "picture"
        val eventDateLong = try {
            eventDate.toLong()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Error parsing event date: ${e.message}")
            Snackbar.make(
                requireView(),
                "Error parsing event date",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val newEvent = Event(
            userId,
            eventIcon,
            eventName,
            eventLocation,
            eventDateLong,
            eventType,
            eventDescription
        )

        userId.let { uid ->
            database.child("events")
                .child(uid)
                .push()
                .key?.let { eventKey ->
                    database.child("events")
                        .child(eventKey)
                        .setValue(newEvent)
                        .addOnSuccessListener {
                            Snackbar.make(
                                requireView(),
                                "Event saved successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            clearInputFields()
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error saving event: ${exception.message}")
                            Snackbar.make(
                                requireView(),
                                "Failed to save event: ${exception.message}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                }
        }
    }

    private fun clearInputFields() {
        binding.apply {
            editTextEventName.text?.clear()
            editTextEventLocation.text?.clear()
            editTextEventDate.text?.clear()
            editTextEventType.text?.clear()
            editTextEventDescription.text?.clear()
            editTextEventName.clearFocus()
            editTextEventLocation.clearFocus()
            editTextEventDate.clearFocus()
            editTextEventType.clearFocus()
            editTextEventDescription.clearFocus()
        }
    }


}











