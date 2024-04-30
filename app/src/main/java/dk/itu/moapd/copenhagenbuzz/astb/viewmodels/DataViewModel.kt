package dk.itu.moapd.copenhagenbuzz.astb.viewmodels

import androidx.core.graphics.convertTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.models.Favorite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {

    private var auth = FirebaseAuth.getInstance()
    private var database = Firebase.database(DATABASE_URL).reference.child("CopenhagenBuzz")


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
            _events.value = makeEvents()
            //_events.value?.let { events -> _favorites.value = generateRandomFavorites(events) }
        }
    }

    private fun makeEvents(): List<Event> {
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

    private fun generateRandomFavorites(events: List<Event>): List<Event> {
        val shuffledIndices = (events.indices).shuffled().take(2).sorted()
        return shuffledIndices.mapNotNull { index -> events.getOrNull(index) }
    }




}



    /*

    fun addToFavorites(event: Event) {

        auth.currentUser?.let { user ->
            val userFavoritesRef = database.child("favorites").child(user.uid)

            // Generate a unique key for the new favorite event
            val newFavoriteEventRef = userFavoritesRef.push()
            val favoriteEventKey = newFavoriteEventRef.key

            // Store the favorite event under the user's favorites using the generated key
            if (favoriteEventKey != null) {
                newFavoriteEventRef.setValue(event)
                    .addOnSuccessListener {
                        // Successfully added the favorite event
                        Log.d(TAG, "Added favorite event to database")
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure to add favorite event
                        Log.e(TAG, "Failed to add favorite event to database", exception)
                    }
            }

            /*
            database
                .child("favorites")
                .child(user.uid)
                .push()
                .key?.let { event ->
                    database.child("favorites")
                        .child(event)
                        .setValue(true)
                }

             */


        }


        /*val favoriteList = _favorites.value?.toMutableList() ?: mutableListOf()
        favoriteList.add(event)
        _favorites.postValue(favoriteList)
        updateFavorites(event, isFavorite = true)

         */
    }


    // Method to remove an event from favorites
    fun removeFromFavorites(event: Event) {
        auth.currentUser?.let { user ->
            val userFavoritesRef = database.child("favorites").child(user.uid)

            // Obtain a reference to the specific favorite event under the user's favorites
            val favoriteEventRef = userFavoritesRef.child(event.eventId)

            // Remove the favorite event from the database
            favoriteEventRef.removeValue()
                .addOnSuccessListener {
                    // Successfully removed the favorite event
                    Log.d(TAG, "Removed favorite event from database")
                }
                .addOnFailureListener { exception ->
                    // Handle failure to remove favorite event
                    Log.e(TAG, "Failed to remove favorite event from database", exception)
                }
        }
    }
}

            /*
            database
                .child("favorites")
                .child(user.uid)
                .push()
                .key?.let { event ->
                    database.child("favorites")
                        .child(event)
                        .removeValue()
                }

             */


            /*
        val favoriteList = _favorites.value?.toMutableList() ?: mutableListOf()
        favoriteList.remove(event)
        _favorites.postValue(favoriteList)
        updateFavorites(event, isFavorite = false)

         */



    private fun updateFavorites(event: Event, isFavorite: Boolean) {
        val updatedEvents = _events.value?.toMutableList() ?: mutableListOf()
        val index = updatedEvents.indexOfFirst { it.userId == event.userId } // Assuming userId is unique
        if (index != -1) {
            updatedEvents[index] = event.copy(isFavorite = isFavorite)
            _events.value = updatedEvents
        }

    }

     */




