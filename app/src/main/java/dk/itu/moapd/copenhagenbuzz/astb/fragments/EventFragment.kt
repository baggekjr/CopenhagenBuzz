package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.Utils.DateFormatter
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.EventLocation
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel
import java.io.File
import java.util.UUID

class EventFragment : Fragment() {
    private var _binding: FragmentEventBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var photoName: String? = null
    private var photoUri: Uri? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private lateinit var dataViewModel: DataViewModel


    private val BUZZ = "CopenhagenBuzz"

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentEventBinding.inflate(inflater, container, false).apply {
        _binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)

        init()
        setListeners()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        database = Firebase.database(DATABASE_URL).reference.child(BUZZ)
        storageReference = Firebase.storage(BUCKET_URL).reference
    }

    private fun setListeners() {
        binding.apply {
            addEventButton.setOnClickListener {
                handleEventButtonOnClick()
            }
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
        }
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoName = "IMG_${UUID.randomUUID()}.JPG"
        val photoFile = photoName?.let { File(requireContext().applicationContext.filesDir, it) }
        photoUri = photoFile?.let {
            FileProvider.getUriForFile(requireContext(), "dk.itu.moapd.copenhagenbuzz.astb.fileprovider",
                it
            )
        }
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
            showMessage("Photo selected!")

            photoUri = uri
            photoName = "IMG_${UUID.randomUUID()}.JPG"

            // Show the user a preview of the photo they just selected
            Picasso.get().load(photoUri).into(binding.eventPhotoPreview)
        } else {
            showMessage("PhotoPicker: No media selected")
        }
    }

    private fun handlePhotoLibraryButtonOnClick() {

        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").build()
        dateRangePicker.show(parentFragmentManager, "DatePicker")
        dateRangePicker.addOnPositiveButtonClickListener { datePicked ->

            startDate = datePicked.first
            endDate = datePicked.second

            //Make sure start and end dates are not null before setting text
            if (startDate != null && endDate != null) {
                val dates = "${DateFormatter.formatDate(startDate!!)} - ${DateFormatter.formatDate(endDate!!)}"
                binding.editTextEventDate.setText(dates)
            }

        }
    }

    private fun handleEventButtonOnClick() {
        val eventName = binding.editTextEventName.text.toString().trim()
        val eventLocationStr = binding.editTextEventLocation.text.toString().replace(' ', '+')
        val eventDate = binding.editTextEventDate.text.toString().trim()
        val eventType = binding.editTextEventType.text.toString().trim()
        val eventDescription = binding.editTextEventDescription.text.toString().trim()

        if (eventName.isNotEmpty() && eventLocationStr.isNotEmpty() && eventDate.isNotEmpty() && eventType.isNotEmpty() && eventDescription.isNotEmpty()) {
            if(photoName != null || photoUri != null) {

            val userId = auth.currentUser?.uid
            userId?.let {
                // Geocode the event location
                val key = "6630a5d972d20365148401gdsd0bcd5"
                val url =
                    "https://geocode.maps.co/search?q=$eventLocationStr+Copenhagen&api_key=$key"

                val queue = Volley.newRequestQueue(requireContext())

                val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
                    response.toString()

                    try {
                        val data = response.getJSONObject(0)
                        val lat = data.getDouble("lat")
                        val lon = data.getDouble("lon")
                        val prettyAddress = formatAddress(data.getString("display_name"))

                        val eventLocation = EventLocation(lat, lon, prettyAddress)

                        dataViewModel.saveEvent(
                            Event(
                                auth.currentUser?.uid,
                                photoName,
                                eventName,
                                eventLocation,
                                startDate,
                                endDate,
                                eventType,
                                eventDescription
                            ),
                            photoUri,
                            {
                                showMessage("Saved event successfully")
                                clearInputFields()
                            },
                            { errorMessage ->
                                showMessage(errorMessage)
                            }
                        )
                    } catch (e: Exception){
                        showMessage("Address not valid. Try again with an address in Copenhagen")
                    }
                }, { error ->
                    handleFailureVolley(error)
                })
                queue.add(request)
            } ?: run {//TODO: this should not be able to happen so do we event need it?
                showMessage("User is not logged in")
            }
            } else {
                showMessage("Please select a photo")
            }
        } else {
            showMessage("Please fill out all fields")

        }
    }
    private fun handleFailureVolley(error: VolleyError?) {
        Log.e(TAG, "error {$error.message}")
        showMessage("Oops! Something went wrong with the network. Please try again later.")
    }

    private fun formatAddress(address: String) : String{
        val list = address
            .split(", ")

        // house number, street name, city name
        return "${list[1]} ${list[0]}, ${list[list.size - 6]}"
    }

    private fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            showMessage("Camera permission denied")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

            eventPhotoPreview.setImageDrawable(null)

        }
    }


    private fun showMessage(s: String){
        Snackbar.make(requireView(), s, Snackbar.LENGTH_SHORT).show()
    }
}
