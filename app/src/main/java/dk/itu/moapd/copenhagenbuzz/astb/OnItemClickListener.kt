package dk.itu.moapd.copenhagenbuzz.astb

import com.google.firebase.database.DatabaseReference
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

/**
 * An interface to implement listener methods for ListView items.
 */
interface OnItemClickListener {

    /**
     * Implement this method to be executed when the user clicks an item in the ListView.
     *
     * @param event An instance of `Dummy` class.
     * @param position The selected position in the ListView.
     */
    fun onItemClickListener(event: Event, position: Int, id: DatabaseReference)
}
