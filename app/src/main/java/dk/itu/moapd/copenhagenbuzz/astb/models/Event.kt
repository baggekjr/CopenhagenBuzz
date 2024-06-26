package dk.itu.moapd.copenhagenbuzz.astb.models

import com.google.firebase.database.IgnoreExtraProperties


/**
 * Data class that takes the information on the Event the user has filled in retrieved from MainActivity
 */
@IgnoreExtraProperties
data class Event(

    val userId: String? = null,
    val eventIcon: String? = null,
    val eventName: String? = null,
    val eventLocation: EventLocation? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val eventType: String? = null,
    var eventDescription: String? = null,

)

