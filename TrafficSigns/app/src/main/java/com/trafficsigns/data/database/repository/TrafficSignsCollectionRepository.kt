package com.trafficsigns.data.database.repository

import androidx.lifecycle.LiveData
import com.trafficsigns.data.database.dao.TrafficSignsCollectionDao
import com.trafficsigns.data.dataclass.TrafficSignsCollection

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * A repository class abstracts access to multiple data sources from MyProfileDatabase.
 * It contains LiveData to keep track of changes in data.
 */
class TrafficSignsCollectionRepository(private val trafficSignsCollectionDao: TrafficSignsCollectionDao) {

    val readAllData: LiveData<List<TrafficSignsCollection>> = trafficSignsCollectionDao.readAllData()

    suspend fun addTrafficSignsCollection(trafficSignsCollection: TrafficSignsCollection){
        trafficSignsCollectionDao.addTrafficSignsCollection(trafficSignsCollection)
    }
}