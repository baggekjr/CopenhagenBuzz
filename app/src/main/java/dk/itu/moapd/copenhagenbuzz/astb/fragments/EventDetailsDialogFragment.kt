package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentEventDetailsDialogBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event


/**
 * A simple [Fragment] subclass.
 * Use the [EventInfoDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventDetailsDialogFragment(private val event: Event) : DialogFragment() {

    private var _binding: FragmentEventDetailsDialogBinding? = null

    private val binding
        get() = requireNotNull(_binding){
            "Cannot access binding because it is null. Is the view visible?"
        }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        _binding = FragmentEventDetailsDialogBinding.inflate(layoutInflater)

        binding.apply {
            editTextEventName.setText(event.eventName)
            editTextEventLocation.setText(event.eventLocation?.address)
            editTextEventDescription.setText(event.eventDescription)
            editTextEventType.setText(event.eventType)
            editTextEventDate.setText(event.startDate.toString())

            return MaterialAlertDialogBuilder(requireContext()).apply {
                setView(binding.root)
                setTitle(R.string.dialog_details_title)
                setNeutralButton(R.string.dialog_close) { dialog, _ -> dialog.dismiss() }
            }.create()
        }

    }
    override fun onPause() {
        super.onPause()
        dismiss()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}