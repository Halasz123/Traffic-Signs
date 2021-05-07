package com.trafficsigns.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.*
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