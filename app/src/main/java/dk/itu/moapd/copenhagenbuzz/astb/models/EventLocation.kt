package dk.itu.moapd.copenhagenbuzz.astb.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class EventLocation (
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null

)