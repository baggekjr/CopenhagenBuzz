package dk.itu.moapd.copenhagenbuzz.astb.interfaces

import com.github.javafaker.Bool
import com.google.firebase.database.DatabaseReference
import dk.itu.moapd.copenhagenbuzz.astb.models.Event

interface OnFavoriteClickListener {
    fun onFavoriteClick(ref: DatabaseReference, isChecked: Boolean)

    fun isFavorite(eventId: String, onResult: (isFavorite: Boolean) -> Unit)


}