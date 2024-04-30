package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentUpdateEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.EventLocation
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel
import java.util.Date
import java.util.Locale
import dk.itu.moapd.copenhagenbuzz.astb.GeocodingHelper
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import io.github.cdimascio.dotenv.dotenv


class UpdateEventDialogFragment(private val event: Event,
                                private val position: Int,
                                private val adapter: EventAdapter) : DialogFragment() {
    private var _binding: FragmentUpdateEventBinding? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference
    private val dataViewModel: DataViewModel by viewModels()
    private lateinit var geocodingHelper: GeocodingHelper
    private lateinit var updatedEvent: Event
    private var eventLocation: EventLocation? = event.eventLocation

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        _binding = FragmentUpdateEventBinding.inflate(layoutInflater)
        //binding.editTextName.setText(dummy.name)
        //auth = FirebaseAuth.getInstance()
        database = Firebase.database(DATABASE_URL).reference.child("CopenhagenBuzz")
        geocodingHelper = GeocodingHelper(requireContext())

        val onPositiveButtonClick: (DialogInterface, Int) -> Unit = { dialog, _ ->
            handleEventButtonOnClick()
            dialog.dismiss()
        }

        binding.apply {
            editTextEventName.setText(event.eventName)
            editTextEventLocation.setText(event.eventLocation?.address)
            editTextEventDescription.setText(event.eventDescription)
            editTextEventType.setText(event.eventType)

            editTextEventDate.setText(event.startDate.toString())
            // Listener for user interaction in the "Add event date" textfield
            editTextEventDate.setOnClickListener {
                handleDateOnClick()
            }

            setupDatePicker()
        }


        return MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setTitle(getString(R.string.dialog_update_eventName))
            setMessage(getString(R.string.dialog_update_eventLocation))
            setPositiveButton(getString(R.string.button_update), onPositiveButtonClick)
            setNegativeButton(getString(R.string.button_cancel)) { dialog, _ -> dialog.dismiss() }
        }.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleEventButtonOnClick() {
            val eventName = binding.editTextEventName.text.toString().trim()
            val eventLocationStr = binding.editTextEventLocation.text.toString().trim()
            val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            // Validate user inputs
            validateInputs(eventName, eventLocationStr, eventDate, eventType, eventDescription)

            /* *//*geocodingHelper.getLocationFromAddress(eventLocationStr) { latitude, longitude ->
                if (latitude != null && longitude != null) {
                    // Create an EventLocation object with the obtained latitude, longitude, and address
                    val eventLocation = EventLocation(latitude, longitude, eventLocationStr)

                    // Create a new Event object with updated location and other details
                    val updatedEvent = Event(
                        userId = event.userId,
                        eventIcon = event.eventIcon,
                        eventName = eventName,
                        eventLocation = eventLocation,
                        startDate = eventDate.toLong(),
                        eventType = eventType,
                        eventDescription = eventDescription
                    )*//*

                    // Perform database operation
                    dataViewModel.editEvent(id, updatedEvent)

                    // Dismiss the dialog after updating the event
                    dismiss()

                }}*/
            val key: String = "6630a5d972d20365148401gdsd0bcd5"

            val url =
                "https://geocode.maps.co/search?q=${eventLocationStr}+Copenhagen&api_key=${key}"

            val queue = Volley.newRequestQueue(activity?.applicationContext)


            val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
                response.toString()

                val data = response.getJSONObject(0)
                val lat = data.getDouble("lat")
                val lon = data.getDouble("lon")
                val prettyAddress = formatAddress(data.getString("display_name"))


                eventLocation = EventLocation(lat, lon, prettyAddress)

                // Save the event
                updatedEvent = Event(
                    userId = event.userId,
                    eventIcon = event.eventIcon,
                    eventName = eventName,
                    eventLocation = eventLocation,
                    startDate = eventDate.toLong(),
                    eventType = eventType,
                    eventDescription = eventDescription
                )
                adapter.getRef(position).setValue(updatedEvent).addOnSuccessListener {
                    "Success"               }.addOnFailureListener {
                    "Error"
                }


            }, { error ->
                handleFailureVolley(error)
            })
            queue.add(request)


    }

private fun formatAddress(address: String) : String{
    val list = address
        .split(", ")

    // house number, street name, city name
    return "${list[1]} ${list[0]}, ${list[list.size - 6]}"
}

    private fun validateInputs(
        eventName: String,
        eventLocation: String,
        eventDate: String,
        eventType: String,
        eventDescription: String
    ) {
        if (eventName.isEmpty() || eventLocation.isEmpty() || eventDate.isEmpty() || eventType.isEmpty() || eventDescription.isEmpty()) {
            throw IllegalArgumentException("Please fill out all fields")
        }
    }

    private fun setupDatePicker() {
                with(binding.editTextEventDate) {
                    keyListener = null
                    setOnFocusChangeListener { _, infocus ->
                        if (infocus) handleDateOnClick()
                    }
                }
            }

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

    private fun handleFailureVolley(error: VolleyError?) {
        Snackbar.make(
            requireView(),
            "VolleyError",
            Snackbar.LENGTH_SHORT
        ).show()
    }



    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }
}
