package com.trafficsigns.data.database.dao

import androidx.room.Dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.trafficsigns.data.dataclass.TrafficSignsCollection

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * It is an interface of TrafficSignsCollectionDatabase and his queries.
 * Contains the methods used for accessing the database.
 */
@Dao
interface TrafficSignsCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrafficSignsCollection(collection: TrafficSignsCollection)

    @Query("SELECT * FROM traffic_signs")
    fun readAllData(): LiveData<List<TrafficSignsCollection>>
}