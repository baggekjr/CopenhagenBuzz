package dk.itu.moapd.copenhagenbuzz.astb.viewmodels

import androidx.core.graphics.convertTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.javafaker.Faker
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {

    private val _events: MutableLiveData<List<Event>> by lazy {
        MutableLiveData<List<Event>>()
    }

    val events: LiveData<List<Event>>
        get() = _events

    init {
        makeEvents()
    }

    private fun makeEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            val faker = Faker()
            val eventList = mutableListOf<Event>()
            for (i in 1..10) {
                val event = Event(
                    userId = faker.number().digit(),
                    eventIcon = faker.avatar().image(),
                    eventName = faker.lorem().word(),
                    eventLocation = faker.address().fullAddress(),
                    startDate = faker.number().randomNumber(),
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