package dk.itu.moapd.copenhagenbuzz.astb.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter


class DeleteEventDialogFragment(private val event: Event,
                                private val position: Int,
                                private val adapter: EventAdapter
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        // Create a lambda for positive button click handling.
        val onPositiveButtonClick: (DialogInterface, Int) -> Unit = { dialog, _ ->
            // Remove an item from the Firebase Realtime database.
            adapter.getRef(position)
                .removeValue()
                .addOnSuccessListener {
                    // Remove the original image.
                    Firebase.storage.reference
                        .child("event")
                        .delete()
                }

            dialog.dismiss()
        }

        // Create and return a new instance of MaterialAlertDialogBuilder.
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.dialog_delete_title))
            setMessage(getString(R.string.dialog_delete_message))
            setPositiveButton(getString(R.string.Delete), onPositiveButtonClick)
            setNegativeButton(getString(R.string.button_cancel)) { dialog, _ -> dialog.dismiss() }
        }.create()
    }


}