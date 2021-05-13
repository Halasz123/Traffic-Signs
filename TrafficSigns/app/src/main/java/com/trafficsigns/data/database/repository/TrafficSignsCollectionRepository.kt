package com.trafficsigns.data.database.repository

import androidx.lifecycle.LiveData
import com.trafficsigns.data.database.dao.TrafficSignsCollectionDao
import com.trafficsigns.data.dataclass.TrafficSignsCollection

class TrafficSignsCollectionRepository(private val trafficSignsCollectionDao: TrafficSignsCollectionDao) {

    val readAllData: LiveData<List<TrafficSignsCollection>> = trafficSignsCollectionDao.readAllData()

    suspend fun addTrafficSignsCollection(trafficSignsCollection: TrafficSignsCollection){
        trafficSignsCollectionDao.addTrafficSignsCollection(trafficSignsCollection)
    }
}