package dk.itu.moapd.copenhagenbuzz.astb.models

/**
 * License??
 */

/**
 * Data class that takes the information on the Event the user has filled in retrieved from MainActivity
 */
data class Event(
    val eventName: String,
    val eventLocation: String,
    val startDate: String,
    val eventType: String,
    var eventDescription: String,
)

