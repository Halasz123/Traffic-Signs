package com.trafficsigns.data

import androidx.room.Dao
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TrafficSignsCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrafficSignsCollection(collection: TrafficSignsCollection)

    @Query("SELECT * FROM traffic_signs")
    fun readAllData(): LiveData<List<TrafficSignsCollection>>
}