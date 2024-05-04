package dk.itu.moapd.copenhagenbuzz.astb.viewmodels

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.core.graphics.convertTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("events")
    private val FAVORITES = "favorites"


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



}

