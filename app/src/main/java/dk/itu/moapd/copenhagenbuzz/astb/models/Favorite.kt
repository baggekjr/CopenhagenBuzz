package dk.itu.moapd.copenhagenbuzz.astb.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Favorite (
    val userId: String? = null,
    val eventId: String? = null,
    val eventIcon: String? = null,
    val eventName: String? = null,
    val eventType: String? = null,
    var isFavorite: Boolean = false

    )