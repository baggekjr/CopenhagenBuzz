package dk.itu.moapd.copenhagenbuzz.astb.viewmodels

import androidx.core.graphics.convertTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import com.google.firebase.database.DatabaseReference
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
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
        getEventsAndFavorites()
    }

    fun editEvent(id: DatabaseReference, event: Event) {
        viewModelScope.launch {
           id.setValue(event)
        }
    }

    private fun getEventsAndFavorites() {
        viewModelScope.launch {
            //_events.value = makeEvents()
            _events.value?.let { events -> _favorites.value = generateRandomFavorites(events) }
        }
    }

    /*
    private fun makeEvents() : List<Event> {
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
        return eventList
    }

     */
    private fun generateRandomFavorites(events: List<Event>): List<Event> {
        val shuffledIndices = (events.indices).shuffled().take(25).sorted()
        return shuffledIndices.mapNotNull { index -> events.getOrNull(index) }
    }








    fun addToFavorites(event: Event) {
        val favoriteList = _favorites.value?.toMutableList() ?: mutableListOf()
        favoriteList.add(event)
        _favorites.postValue(favoriteList)
    }

    // Method to remove an event from favorites
    fun removeFromFavorites(event: Event) {
        val favoriteList = _favorites.value?.toMutableList() ?: mutableListOf()
        favoriteList.remove(event)
        _favorites.postValue(favoriteList)
    }


}