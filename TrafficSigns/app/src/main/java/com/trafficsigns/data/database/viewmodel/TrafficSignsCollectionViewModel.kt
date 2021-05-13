package com.trafficsigns.data.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.*
import com.trafficsigns.data.database.room.TrafficSignsCollectionDatabase
import com.trafficsigns.data.database.repository.TrafficSignsCollectionRepository
import com.trafficsigns.data.dataclass.TrafficSignsCollection
import kotlinx.coroutines.*

class TrafficSignsCollectionViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<TrafficSignsCollection>>
    private val repository: TrafficSignsCollectionRepository

    init {
        val trafficSignsCollectionDao = TrafficSignsCollectionDatabase.getDatabase(application).trafficSignsCollectionDao()
        repository = TrafficSignsCollectionRepository(trafficSignsCollectionDao)
        readAllData = repository.readAllData
    }

    fun addTrafficSignsCollection(trafficSignsCollection: TrafficSignsCollection){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTrafficSignsCollection(trafficSignsCollection = trafficSignsCollection)
        }
    }
}