package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.Utils.DateFormatter
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentUpdateEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.EventLocation
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel
import io.github.cdimascio.dotenv.dotenv
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * A dialogFragment subclass for updating an event. It allows users to edit event details such as name,
 * location, date, type, and description,and update them in the database by using an instance of the DataViewModel.
 * Users can also change event images by capturing a new photo or selecting from the device's gallery.
 */

class UpdateEventDialogFragment(
    private val event: Event,
    private val position: Int,
    private val adapter: EventAdapter, private val parentView: View
) : DialogFragment() {
    private var _binding: FragmentUpdateEventBinding? = null
    private lateinit var database: DatabaseReference
    private lateinit var updatedEvent: Event
    private var eventLocation: EventLocation? = event.eventLocation
    private val storageReference = Firebase.storage(BUCKET_URL).reference
    private var photoName: String? = null
    private var photoUri: Uri? = null
    private val BUZZ = "CopenhagenBuzz"
    private var startDate: Long? = event.startDate
    private var endDate: Long? = event.endDate
    private val dateFormatter = SimpleDateFormat("EEE dd/MM/yyyy", Locale.ENGLISH)
    private lateinit var dataViewModel: DataViewModel


    //Boolean to make sure it does not dismiss dialog when taking picture
    private var cameraActive = false

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        _binding = FragmentUpdateEventBinding.inflate(layoutInflater)
        database = Firebase.database(DATABASE_URL).reference.child(BUZZ)
        dataViewModel = ViewModelProvider(requireActivity()).get(DataViewModel::class.java)


        val onPositiveButtonClick: (DialogInterface, Int) -> Unit = { dialog, _ ->
            handleEventButtonOnClick()
            dialog.dismiss()
        }

        binding.apply {
            editTextEventName.setText(event.eventName)
            editTextEventLocation.setText(event.eventLocation?.address)
            editTextEventDescription.setText(event.eventDescription)
            editTextEventType.setText(event.eventType)
            //Check if either start or end date is null:
            val formattedStartDate = startDate?.let { dateFormatter.format(Date(it)) } ?: ""
            val formattedEndDate = endDate?.let { dateFormatter.format(Date(it)) } ?: ""
            val formattedDates = "$formattedStartDate - $formattedEndDate"
            editTextEventDate.setText(formattedDates)
            photoName = event.eventIcon

            editTextEventDate.setOnClickListener {
                handleDateOnClick()
            }

            eventCamera.setOnClickListener {
                if (checkCameraPermission()) {
                    launchCamera()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
            eventPhotoLibrary.setOnClickListener {
                cameraActive = true
                handlePhotoLibraryButtonOnClick()
            }

            setupDatePicker()

            storageReference.child(event.eventIcon!!).downloadUrl
                .addOnSuccessListener { uri ->
                    Picasso.get().load(uri).into(eventPhotoPreview)
                    photoUri = uri
                }
                .addOnFailureListener { ex ->
                    handleFailure(ex)
                }
        }


        return MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setTitle(getString(R.string.dialog_update_eventName))
            setPositiveButton(getString(R.string.button_update), onPositiveButtonClick)
            setNegativeButton(getString(R.string.button_cancel)) { dialog, _ -> dialog.dismiss() }

        }.create()
    }

    override fun onPause() {
        super.onPause()
        if (!cameraActive) {
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Method to launch the camera to capture a photo.
     * It generates a unique photo name using UUID and creates a file to store the captured photo.
     * It also configures an intent to capture an image and save it to the specified UR and launches
     * the camera activity with the configured intent.
     */
    private fun launchCamera() {
        cameraActive = true
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoName = "IMG_${UUID.randomUUID()}.JPG"
        val photoFile = photoName?.let { File(requireContext().applicationContext.filesDir, it) }
        photoUri = photoFile?.let {
            FileProvider.getUriForFile(
                requireContext(),
                "dk.itu.moapd.copenhagenbuzz.astb.fileprovider",
                it
            )
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        takePhoto.launch(cameraIntent)
    }

    /**
     * Activity result launcher for capturing a photo.
     * Handles the result of the camera activity.
     */
    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Picasso.get().load(photoUri).into(binding.eventPhotoPreview)
            // Update the photo name immediately
            photoName = "IMG_${UUID.randomUUID()}.JPG"
        }
        cameraActive = false
    }

    /**
     * Activity result launcher for picking media from the device's gallery.
     * Handles the result of the media selection activity.
     */
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            showMessage("Photo selected!")
            photoUri = uri
            photoName = "IMG_${UUID.randomUUID()}.JPG"

            // Show the user a preview of the photo they just selected
            Picasso.get().load(photoUri).into(binding.eventPhotoPreview)

        } else {
            showMessage("PhotoPicker: No media selected")
        }
        cameraActive = false
    }


    /**
     * Handles the button click event to open the device's gallery for selecting a photo.
     * Launches the activity to pick visual media, specifically images.
     */
    private fun handlePhotoLibraryButtonOnClick() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    /**
     * Method to handle event being added. This method uses the DataViewModel method saveEvent() to add
     * add the event to the database and the image to the storage bucket. It sets the
     * eventLocation by using forward geocoding from:
     * https://geocode.maps.co/
     */
    private fun handleEventButtonOnClick() {
        try {
            val eventName = binding.editTextEventName.text.toString().trim()
            val eventLocationStr = binding.editTextEventLocation.text.toString()
                .replace(' ', '+')
            val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            // Validate user inputs
            validateInputs(eventName, eventLocationStr, eventDate, eventType, eventDescription)

            val key: String = dotenv {
                directory = "/assets"
                filename = "env"
            }["GEOCODE_API_KEY"] ?: throw IllegalArgumentException("GEOCODE_API_KEY not found in env file")

            val url =
                "https://geocode.maps.co/search?q=${eventLocationStr}+Copenhagen&api_key=${key}"

            val queue = Volley.newRequestQueue(activity?.applicationContext)


            val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
                response.toString()

                try {
                    val data = response.getJSONObject(0)
                    val lat = data.getDouble("lat")
                    val lon = data.getDouble("lon")
                    val prettyAddress = formatAddress(data.getString("display_name"))


                    eventLocation = EventLocation(lat, lon, prettyAddress)

                    // Save the updated event
                    updatedEvent = Event(
                        userId = event.userId,
                        eventIcon = photoName,
                        eventName = eventName,
                        eventLocation = eventLocation,
                        startDate = startDate,
                        endDate = endDate,
                        eventType = eventType,
                        eventDescription = eventDescription
                    )

                    // Call the updateEvent method in DataViewModel
                    dataViewModel.updateEvent(
                        adapter.getId(position),
                        updatedEvent,
                        photoUri,
                        event.eventIcon,
                        adapter
                    )
                    adapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    showMessage("Address not valid. Try updating your again event again with an address in Copenhagen.${e.message}")
                    Log.e(TAG, "${e.message}")
                }
            }, { error ->
                handleFailureVolley(error)
            })
            queue.add(request)

        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException: ${e.message}")
            showMessage("Oops! Something unexpected happened while updating your event. Please try again.")
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }

    }

    private fun formatAddress(address: String): String {
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
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").build()
        dateRangePicker.show(parentFragmentManager, "DatePicker")
        dateRangePicker.addOnPositiveButtonClickListener { datePicked ->

            startDate = datePicked.first
            endDate = datePicked.second

            //Check if dates are not null before setting text
            if (startDate != null && endDate != null) {
                val dates =
                    "${DateFormatter.formatDate(startDate!!)} - ${DateFormatter.formatDate(endDate!!)}"
                binding.editTextEventDate.setText(dates)
            }

        }
    }

    private fun handleFailureVolley(error: VolleyError?) {
        Log.e(TAG, "VolleyError: $error")
        showMessage("Oops! Something went wrong with the network. Please try again later.")

    }

    private fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                showMessage("Camera permission denied")
            }
        }

    private fun handleFailure(exception: Exception) {
        Log.e(TAG, "Database save failure with following exception: $exception")

    }

    private fun showMessage(s: String) {
        Snackbar.make(parentView, s, Snackbar.LENGTH_SHORT).show()
    }

}
