
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel

class DeleteEventDialogFragment(
    private val event: Event,
    private val position: Int,
    private val adapter: EventAdapter

) : DialogFragment(){

    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val storage = Firebase.storage(BUCKET_URL).reference
        val ref = adapter.getRef(position)

        val onPositiveButtonClick: (DialogInterface, Int) -> Unit = { dialog, _ ->
            ref
                .removeValue()
                .addOnSuccessListener { //TODO: HANDLE FAILURE AND SUCCESS
                            println(storage.child(event.eventIcon!!).toString())
                            storage.child(event.eventIcon!!).delete()
                        }
                    ref.key?.let { dataViewModel.removeEventFromFavorites(it) }
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

    override fun onPause() {
        super.onPause()
        dismiss()
    }


}
