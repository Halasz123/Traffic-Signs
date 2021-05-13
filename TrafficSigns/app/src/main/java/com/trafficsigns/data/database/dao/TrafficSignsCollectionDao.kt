package com.trafficsigns.data.database.dao

import androidx.room.Dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.trafficsigns.data.dataclass.TrafficSignsCollection

@Dao
interface TrafficSignsCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrafficSignsCollection(collection: TrafficSignsCollection)

    @Query("SELECT * FROM traffic_signs")
    fun readAllData(): LiveData<List<TrafficSignsCollection>>
}