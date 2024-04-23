import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

class DeleteEventDialogFragment(
    private val event: Event,
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
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.event_deleted),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            handleError(e)
                        }
                }
                .addOnFailureListener { e ->
                    handleError(e)
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

    //Handle possible errors
    private fun handleError(exception: Exception) {
        when (exception) {
            is FirebaseNetworkException -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.network_error_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
            is FirebaseAuthException -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.auth_error_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
            is StorageException -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.storage_error_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.generic_error_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
