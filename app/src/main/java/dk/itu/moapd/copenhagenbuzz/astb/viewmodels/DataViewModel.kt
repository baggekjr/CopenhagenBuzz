package dk.itu.moapd.copenhagenbuzz.astb.viewmodels

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.copenhagenbuzz.astb.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("events")
    private val FAVORITES = "favorites"
    private val storageReference = Firebase.storage(BUCKET_URL).reference


    private var auth = FirebaseAuth.getInstance()
    private var database = Firebase.database(DATABASE_URL).reference.child("CopenhagenBuzz")


    private val _events: MutableLiveData<List<Event>> by lazy {
        MutableLiveData<List<Event>>()

    }


    private val _favorites: MutableLiveData<List<Event>> by lazy {
        MutableLiveData<List<Event>>()
    }

    val favorites: LiveData<List<Event>>
        get() = _favorites




    fun editEvent(id: DatabaseReference, event: Event) {
        viewModelScope.launch {
            id.setValue(event)
        }
    }


    fun getEvents(): LiveData<List<Event>> {
        val mutableLiveData = MutableLiveData<List<Event>>()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "DataSnapshot: $snapshot")

                val events = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    Log.e(TAG, "$eventSnapshot: EVENT!!!!")
                    val event = eventSnapshot.getValue(Event::class.java)
                    event?.let {
                        events.add(it)
                    }
                }
                mutableLiveData.value = events
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        return mutableLiveData
    }

    fun fetchEvents() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    event?.let {
                        events.add(it)
                    }
                }
                _events.value = events
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun isFavorite(eventId: String, onResult: (isFavorite: Boolean) -> Unit) {

        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                database.child(FAVORITES).child(userId).child(eventId).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            onResult(true)
                        } else {
                            onResult(false)
                        }
                    }
                    override fun onCancelled(error: DatabaseError){
                        Log.d(TAG, error.message)
                        onResult(false)
                    }
                })
            }
        }

    }


    /**
     * Method to make sure if an event is deleted from the database, that the event is also deleted from
     * the list of favorites under all the users that have that event favorited
     *
     * @param deletedEvent is a String that represents a reference key, which is the eventId for the
     * deleted event.
     */
    fun removeEventFromFavorites(deletedEvent: String) {
        val favoritesReference = database.child(FAVORITES)


        favoritesReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {

                    userSnapshot.child(deletedEvent).ref.removeValue()
                        .addOnSuccessListener {
                            // Event successfully removed from user's favorites
                            Log.d(TAG, "Event $deletedEvent removed from user ${userSnapshot.key}'s favorites")
                        }
                        .addOnFailureListener { error ->
                            // Handle failure to remove event from user's favorites
                            Log.e(TAG, "Failed to remove event $deletedEvent from user ${userSnapshot.key}'s favorites", error)
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e(TAG, "Database operation cancelled", error.toException())
            }
        })

    }


    /**
     * Method to determine if the user is removing or adding an event to their favorites depending on
     * if the checkBox heart is already checked or not.
     *
     * @param ref takes the reference to the event that is favorited or not
     * @param isChecked Boolean to give information; if it's already checked, when pressed it need to uncheck and remove
     * from favorites. If not checked it need to add
     */

    fun updateFavorite(ref: DatabaseReference, isChecked: Boolean) {
        if (isChecked) {
            addFavorite(ref)
        } else {
            removeFavorite(ref)
        }

    }

    private fun addFavorite(ref: DatabaseReference) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->

                ref.key?.let {
                    database.child(FAVORITES).child(userId).child(it).setValue(true).addOnSuccessListener {
                        Log.d(TAG, "Favorited event succesfully")
                    }.addOnFailureListener {
                        Log.d(TAG, "An error occurred: $it")
                    }
                }

            }
        }
    }

    private fun removeFavorite(ref: DatabaseReference) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                ref.key?.let {
                    database.child(FAVORITES).child(userId).child(it).removeValue()
                        .addOnSuccessListener {
                            Log.d(TAG, "Unfavorited event succesfully")
                        }.addOnFailureListener {
                        Log.d(TAG, "An error occurred: $it")
                    }
                }

            }
        }

    }

    /**
     * Method to update the selected event.
     *
     * @param ref takes the reference to the event that is being updated
     * @param updatedEvent The updated event.
     * @param updatedPhotoUri the uri used to update the picture of the event if needed
     * @param oldPhotoName the name for the photo from the not yet updated event
     * @param adapter passing the adapter as to notify the adapter about dataset changes
     * to make the picture, and the rest of the event info, update without reload
     */

    fun updateEvent(ref: DatabaseReference, updatedEvent: Event, updatedPhotoUri: Uri?, oldPhotoName: String?, adapter: EventAdapter) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                ref.key?.let { eventKey ->
                    // Check if the photo has been updated
                    Log.e(TAG, "$oldPhotoName = ${updatedEvent.eventIcon} er de det samme")
                    if (updatedPhotoUri != null && updatedEvent.eventIcon != null && oldPhotoName != updatedEvent.eventIcon) {
                        storageReference.child(updatedEvent.eventIcon)
                            .putFile(updatedPhotoUri)
                            .addOnSuccessListener {
                                Log.d(TAG, "Photo uploaded successfully!")
                                adapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { ex ->
                                Log.e(TAG, "Photo upload with exception: $ex")
                                // Handle failure
                            }

                        // Delete the old photo if it exists
                        updatedEvent.eventIcon?.let {storageReference.child(oldPhotoName!!).delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Successfully deleted old photo!")
                                    // After photo deletion, update the event data
                                    updateEventData(eventKey, updatedEvent, adapter)
                                }
                                .addOnFailureListener { ex ->
                                    Log.e(TAG, "Failed to delete old photo: $ex")
                                    // Handle failure
                                    // Even if there's a failure, we should still attempt to update the event data
                                    updateEventData(eventKey, updatedEvent, adapter)
                                }
                        }
                    } else {
                        // If the photo hasn't been updated, just update the event data
                        updateEventData(eventKey, updatedEvent, adapter)
                    }
                }
            }
        }
    }

    private fun updateEventData(eventKey: String, updatedEvent: Event, adapter: EventAdapter) {
        // Update event data
        database.child("events").child(eventKey).setValue(updatedEvent)
            .addOnSuccessListener {
                Log.d(TAG, "Event updated successfully")
                adapter.notifyDataSetChanged() // Notify adapter

            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to update event", exception)
            }
    }

}

