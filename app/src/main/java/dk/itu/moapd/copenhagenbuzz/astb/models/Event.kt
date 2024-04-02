package dk.itu.moapd.copenhagenbuzz.astb.models

import android.graphics.drawable.Icon


/**
 * Data class that takes the information on the Event the user has filled in retrieved from MainActivity
 */
data class Event(
    val userID: Int,
    val eventIcon: String,
    val eventName: String,
    val eventLocation: String,
    val startDate: String,
    val eventType: String,
    var eventDescription: String,
)

