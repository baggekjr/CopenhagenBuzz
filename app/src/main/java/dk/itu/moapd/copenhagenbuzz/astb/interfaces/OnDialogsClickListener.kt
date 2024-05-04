package dk.itu.moapd.copenhagenbuzz.astb.interfaces

import dk.itu.moapd.copenhagenbuzz.astb.models.Event

interface OnDialogsClickListener {
    fun onDeleteEvent(event: Event, position: Int)

    fun onEditEvent(event: Event, position: Int)
}