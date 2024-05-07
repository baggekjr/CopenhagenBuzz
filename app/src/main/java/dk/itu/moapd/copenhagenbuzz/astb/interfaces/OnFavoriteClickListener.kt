package dk.itu.moapd.copenhagenbuzz.astb.interfaces

import com.google.firebase.database.DatabaseReference

/**
 * An interface to implement listener methods for onFavorite.
 */
interface OnFavoriteClickListener {
    fun onFavoriteClick(ref: DatabaseReference, isChecked: Boolean)

    fun isFavorite(eventId: String, onResult: (isFavorite: Boolean) -> Unit)


}