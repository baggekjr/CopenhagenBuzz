package dk.itu.moapd.copenhagenbuzz.astb.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.javafaker.Faker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {

    private val _events: MutableLiveData<List<Event>> by lazy {
        MutableLiveData<List<Event>>()
    }
    private val _favorites: MutableLiveData<List<Event>> by lazy {
        MutableLiveData<List<Event>>()
    }

    val events: LiveData<List<Event>>
        get() = _events

    val favorites: LiveData<List<Event>>
        get() = _favorites


    init {

    }

    private fun generateRandomFavorites(events: List<Event>): List<Event> {
        val shuffledIndices = (events.indices).shuffled().take(25).sorted()
        return shuffledIndices.mapNotNull { index -> events.getOrNull(index) }
    }

    init {
        makeEvents()
    }

    private fun makeEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            val faker = Faker()
            val eventList = mutableListOf<Event>()
            for (i in 1..10) {
                val event = Event(
                    eventIcon = faker.avatar().image(),
                    eventName = faker.lorem().word(),
                    eventLocation = faker.address().fullAddress(),
                    startDate = faker.lorem().word(),
                    eventType = faker.lorem().word(),
                    eventDescription = faker.lorem().paragraph()

                )
                eventList.add(event)
                println(eventList)
            }
            _events.postValue(eventList)
        }
    }

}