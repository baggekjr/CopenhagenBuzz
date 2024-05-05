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
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.Utils.DateFormatter
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentUpdateEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.EventLocation
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.UUID


class UpdateEventDialogFragment(private val event: Event,
                                private val position: Int,
                                private val adapter: EventAdapter, private val parentView: View) : DialogFragment() {
    private var _binding: FragmentUpdateEventBinding? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference
    private val dataViewModel: DataViewModel by viewModels()
    private lateinit var updatedEvent: Event
    private var eventLocation: EventLocation? = event.eventLocation
    private val storageReference = Firebase.storage(BUCKET_URL).reference
    private var photoName: String? = null
    private var photoUri: Uri? = null
    private val BUZZ = "CopenhagenBuzz"
    private var startDate: Long? = event.startDate
    private var endDate: Long? = event.endDate
    private val dateFormatter = SimpleDateFormat("EEE dd/MM/yyyy", Locale.ENGLISH)


    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        _binding = FragmentUpdateEventBinding.inflate(layoutInflater)
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

    private fun readApiKey(): String {
        val properties = Properties()
        val propertiesFile = File(requireContext().filesDir, "local.properties")
        properties.load(FileInputStream(propertiesFile))
        return properties.getProperty("geocode_api_key")
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
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            photoName = "IMG_${UUID.randomUUID()}.JPG"

            Picasso.get().load(photoUri).into(binding.eventPhotoPreview)
        } else {
            showMessage("PhotoPicker: No media selected")
        }
    }



    private fun handlePhotoLibraryButtonOnClick() {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private fun handleEventButtonOnClick() {
        try{
            val eventName = binding.editTextEventName.text.toString().trim()
        val eventLocationStr = binding.editTextEventLocation.text.toString()
            .replace(' ', '+')
        val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            // Validate user inputs
            validateInputs(eventName, eventLocationStr, eventDate, eventType, eventDescription)

            val apiKey  = readApiKey()

            val url =
                "https://geocode.maps.co/search?q=${eventLocationStr}+Copenhagen&api_key=${apiKey}"

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
                    if (photoName != event.eventIcon) {
                        storageReference.child(photoName!!)
                            .putFile(photoUri!!)
                            .addOnSuccessListener {
                                println("Photo uploaded successfully!")
                                //Makes picture get updated without having to reload page
                                adapter.notifyDataSetChanged()

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
                    showMessage( "Address not valid. Try again with an address in Copenhagen.")
                }
                }, { error ->
                handleFailureVolley(error)
            })
            queue.add(request)

        } catch (e: IllegalStateException) {
            // Handle the IllegalStateException here
            Log.e(TAG, "IllegalStateException: ${e.message}")
            // You can show an error message to the user or handle it in any other appropriate way
        } catch (e: Exception) {
            // Handle other exceptions here if needed
            Log.e(TAG, "Exception: ${e.message}")
        }

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
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").build()
        dateRangePicker.show(parentFragmentManager, "DatePicker")
        dateRangePicker.addOnPositiveButtonClickListener { datePicked ->

            startDate = datePicked.first
            endDate = datePicked.second

            //Check if dates are not null before setting text
            if (startDate != null && endDate != null) {
                val dates = "${DateFormatter.formatDate(startDate!!)} - ${DateFormatter.formatDate(endDate!!)}"
                binding.editTextEventDate.setText(dates)
            }

        }
    }

    private fun handleFailureVolley(error: VolleyError?) {
        showMessage("VolleyError")
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

    private fun handleFailure(exception: Exception) {
        println("Database save failure with following exception: $exception")

    }

    private fun showMessage( s: String){
        Snackbar.make(parentView, s, Snackbar.LENGTH_SHORT).show()
    }

}
