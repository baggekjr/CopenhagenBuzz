package dk.itu.moapd.copenhagenbuzz.astb.models

data class Event(
    val eventName: String,
    val eventLocation: String,
    val startDate: String,
    val eventType: String,
    var eventDescription: String,
)

