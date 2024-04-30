package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.icu.text.SimpleDateFormat
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import dk.itu.moapd.copenhagenbuzz.astb.GeocodingHelper
import dk.itu.moapd.copenhagenbuzz.astb.models.EventLocation
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.UUID


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
    private var photoName: String? = null
    private var photoUri: Uri? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        // This is what runs after the camera activity finishes
        if (didTakePhoto && photoName != null) {
            //showMessage("Photo taken!")

            // Show the user a preview of the photo they just took
            Picasso.get().load(photoUri).into(binding.eventPhotoPreview)
        }
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
            eventCamera.setOnClickListener {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoName = "IMG_${UUID.randomUUID()}.JPG"
                val photoFile = File(
                    requireContext().applicationContext.filesDir,
                    photoName
                )
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "dk.itu.moapd.copenhagenbuzz.astb.fileprovider",
                    photoFile
                )
                takePhoto.launch(photoUri)
            }
                setupDatePicker()}

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
                .isNotEmpty()
        ) {

            val eventName = binding.editTextEventName.text.toString().trim()
            val eventLocationStr = binding.editTextEventLocation.text.toString().replace(' ', '+')
            val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()
            val eventIcon = photoName
            val eventIconUri: Uri? = photoUri

            if (eventName.isNotEmpty() && eventLocationStr.isNotEmpty() && eventDate.isNotEmpty() && eventType.isNotEmpty() && eventDescription.isNotEmpty()) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    // Geocode the event location
                    val key: String = "6630a5d972d20365148401gdsd0bcd5"
                    val url =
                        "https://geocode.maps.co/search?q=$eventLocationStr+Copenhagen&api_key=$key"

                    val queue = Volley.newRequestQueue(activity?.applicationContext)

                    val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
                        response.toString()

                        val data = response.getJSONObject(0)
                        val lat = data.getDouble("lat")
                        val lon = data.getDouble("lon")
                        val prettyAddress = formatAddress(data.getString("display_name"))

                        val eventLocation = EventLocation(lat, lon, prettyAddress)

                        // Save the event
                        saveEventWithImage(
                            userId,
                            eventIconUri,
                            eventName,
                            eventLocation,
                            eventDate,
                            eventType,
                            eventDescription
                        )
                    }, { error ->
                        handleFailureVolley(error)
                    })
                    queue.add(request)
                } else {
                    Snackbar.make(requireView(), "User not logged in", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(requireView(), "Please fill out all fields", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun handleFailureVolley(error: VolleyError?) {
        Snackbar.make(
            requireView(),
            "error {$error.message}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun saveEventWithImage(
        userId: String,
        eventIcon: Uri?, // Image URI to upload
        eventName: String,
        eventLocation: EventLocation,
        eventDate: String,
        eventType: String,
        eventDescription: String
    ) {
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

        // Upload the image to Firebase Storage
        if (eventIcon != null) {
            val photoName = "IMG_${UUID.randomUUID()}.jpg"
            val photoRef = Firebase.storage.reference.child("images/$photoName")

            photoRef.putFile(eventIcon)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully, get the download URL
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        // URL for the uploaded image
                        val imageUrl = uri.toString()

                        // Create new Event object with image URL
                        val newEvent = Event(
                            userId,
                            imageUrl,
                            eventName,
                            eventLocation,
                            eventDateLong,
                            eventType,
                            eventDescription
                        )

                        // Save the event to Firebase Realtime Database
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
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error uploading image: ${exception.message}")
                    Snackbar.make(
                        requireView(),
                        "Failed to upload image: ${exception.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        } else {
            Snackbar.make(
                requireView(),
                "Please take a photo",
                Snackbar.LENGTH_SHORT
            ).show()
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









