package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentUpdateEventBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel

class UpdateEventDialogFragment(private val event: Event, private val position: Int, private val id: DatabaseReference) : DialogFragment() {
    private var _binding: FragmentUpdateEventBinding? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference
    private val dataViewModel: DataViewModel by viewModels()

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

        val onPositiveButtonClick: (DialogInterface, Int) -> Unit = { dialog, _ ->
            handleEventButtonOnClick()
            dialog.dismiss()
        }

        binding.apply {
            val name = editTextEventName.setText(event.eventName)
            val loc = editTextEventLocation.setText(event.eventLocation)
            val des = editTextEventDescription.setText(event.eventDescription)
            val type = editTextEventType.setText(event.eventType)

            // Convert the start date (which is in milliseconds) to a readable date format
            val startDateString = event.startDate?.toString() ?: ""



// Set the formatted start date to the EditText
            val date = binding.editTextEventDate.setText(startDateString)


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

        //val userId = auth.currentUser!!.uid

            val eventName = binding.editTextEventName.text.toString().trim()
            val eventLocation = binding.editTextEventLocation.text.toString().trim()
            val eventDate = binding.editTextEventDate.text.toString().trim()
            val eventType = binding.editTextEventType.text.toString().trim()
            val eventDescription = binding.editTextEventDescription.text.toString().trim()

            // Create a new Event object with updated eventName
            val updatedEvent = Event(
                userId = event.userId,
                eventIcon = event.eventIcon,
                eventName = eventName,
                eventLocation = eventLocation,
                //startDate = eventDate.toLong(),
                eventType = eventType,
                eventDescription = eventDescription
            )
            dataViewModel.editEvent(id, event)

            // Update the event in the Firebase database
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                database.child("events")
                    .child(uid)
                    .setValue(updatedEvent)
            }

            /*Snackbar.make(
                view,
                "Event updated successfully",
                Snackbar.LENGTH_SHORT
            ).show()*/

            dismiss() // Dismiss the dialog after updating the event
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

                val date = datePicked.first + datePicked.second

                setText(date.toString())
            }
        }
    }
    private fun showMessage(event: String) {
       // Log.d(EventFragment.TAG, event)
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
