package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
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
import androidx.fragment.app.viewModels
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.GeocodingHelper
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentUpdateEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.EventLocation
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel
import java.io.File
import java.util.Date
import java.util.Locale
import java.util.UUID


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
    private val storageReference = Firebase.storage(BUCKET_URL).reference
    private var photoName: String? = null
    private var photoUri: Uri? = null
    private val BUZZ = "CopenhagenBuzz"
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        _binding = FragmentUpdateEventBinding.inflate(layoutInflater)
        //binding.editTextName.setText(dummy.name)
        //auth = FirebaseAuth.getInstance()
        database = Firebase.database(DATABASE_URL).reference.child(BUZZ)

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoName = "IMG_${UUID.randomUUID()}.JPG"
        val photoFile = File(requireContext().applicationContext.filesDir, photoName)
        photoUri = FileProvider.getUriForFile(requireContext(), "dk.itu.moapd.copenhagenbuzz.astb.fileprovider", photoFile)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        takePhoto.launch(cameraIntent)
    }



    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Picasso.get().load(photoUri).into(binding.eventPhotoPreview)
        }
    }

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> // Callback is invoked after the user selects a media item or closes the photo picker.
        if (uri != null) {
            //showMessage("Photo selected!")

            photoUri = uri
            photoName = "IMG_${UUID.randomUUID()}.JPG"

            // Show the user a preview of the photo they just selected
            Picasso.get().load(photoUri).into(binding.eventPhotoPreview)
        } else {
            //showMessage("PhotoPicker: No media selected")
        }
    }


    private fun handlePhotoLibraryButtonOnClick() {

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    }


    private fun handleEventButtonOnClick() {
            val eventName = binding.editTextEventName.text.toString().trim()
        val eventLocationStr = binding.editTextEventLocation.text.toString()
            .replace(' ', '+')
        val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            // Validate user inputs
            validateInputs(eventName, eventLocationStr, eventDate, eventType, eventDescription)

            val key: String = "6630a5d972d20365148401gdsd0bcd5"

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
                        startDate = eventDate.toLong(),
                        eventType = eventType,
                        eventDescription = eventDescription
                    )
                    Log.d(TAG, "Updated event: $updatedEvent")

                    if (photoName != event.eventIcon) {
                        storageReference.child(photoName!!)
                            .putFile(photoUri!!)
                            .addOnSuccessListener {
                                println("Photo uploaded successfully!")
                            }.addOnFailureListener {
                                println("Photo upload failed with exception: $it")
                                handleFailure(it)
                            }

                        storageReference.child(event.eventIcon!!).delete()
                            .addOnSuccessListener { ex ->
                                println("Successfully deleted old photo!")
                            }
                            .addOnFailureListener { ex ->
                                handleFailure(ex)
                            }
                    }
                    adapter.getRef(position).setValue(updatedEvent)
                        .addOnSuccessListener {
                            // Event updated successfully
                            Log.d(TAG, "Event updated successfully!")
                        }
                        .addOnFailureListener {
                            // Error updating event
                            Log.e(TAG, "Error updating event: $it")
                            handleFailure(it)
                        }
                }catch(e: Exception){
                    showMessage("Address not valid. Should be written in format'Street House, City'")
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

    private fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Snackbar.make(requireView(), "Camera permission denied", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleFailure(exception: Exception) {
        println("Database save failure with following exception: $exception")

    }

    private fun showMessage(s: String){
        Snackbar.make(requireView(), "Camera permission denied", Snackbar.LENGTH_SHORT).show()
    }

}
