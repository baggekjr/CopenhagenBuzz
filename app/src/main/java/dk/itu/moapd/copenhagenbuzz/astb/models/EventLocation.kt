package dk.itu.moapd.copenhagenbuzz.astb.models

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Data class that defines the event location with a latitude, longitude and an address
 */
@IgnoreExtraProperties
data class EventLocation (
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null

)