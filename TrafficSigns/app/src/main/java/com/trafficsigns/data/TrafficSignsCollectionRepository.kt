package com.trafficsigns.data

import androidx.lifecycle.LiveData

class TrafficSignsCollectionRepository(private val trafficSignsCollectionDao: TrafficSignsCollectionDao) {

    val readAllData: LiveData<List<TrafficSignsCollection>> = trafficSignsCollectionDao.readAllData()

    suspend fun addTrafficSignsCollection(trafficSignsCollection: TrafficSignsCollection){
        trafficSignsCollectionDao.addTrafficSignsCollection(trafficSignsCollection)
    }
}