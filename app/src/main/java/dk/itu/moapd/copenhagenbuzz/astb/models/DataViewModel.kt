package dk.itu.moapd.copenhagenbuzz.astb.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataViewModel : ViewModel() {

    private val _events: MutableLiveData<List<Event>> by lazy {
        MutableLiveData<List<Event>>()
    }

    val events: LiveData<List<Event>>
        get() = _events
}