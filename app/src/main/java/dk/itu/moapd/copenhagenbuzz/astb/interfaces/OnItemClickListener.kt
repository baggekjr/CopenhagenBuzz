package dk.itu.moapd.copenhagenbuzz.astb.interfaces

import dk.itu.moapd.copenhagenbuzz.astb.models.Event

/**
 * An interface to implement listener methods for ListView items.
 */

interface OnItemClickListener {

    /**
     * Implement this method to be executed when the user clicks an item in the ListView.
     *
     * @param event An instance of `Event` class.
     * @param position The selected position in the ListView.
     */
    fun onDeleteEvent(event: Event, position: Int)

    fun onEditEvent(event: Event, position: Int)
}

