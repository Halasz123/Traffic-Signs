package com.trafficsigns.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trafficsigns.data.database.Convertes
import com.trafficsigns.data.database.dao.TrafficSignsCollectionDao
import com.trafficsigns.data.dataclass.TrafficSignsCollection
import com.trafficsigns.ui.constant.Data

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * Contains the database holder and serves as the main access point
 * for the underlying connection to your app's persisted, relational data.
 */
@Database(entities = [TrafficSignsCollection::class], version = 1, exportSchema = false)
@TypeConverters(Convertes::class)
abstract  class TrafficSignsCollectionDatabase: RoomDatabase() {

    abstract fun trafficSignsCollectionDao(): TrafficSignsCollectionDao

    companion object{
        @Volatile
        private var INSTANCE: TrafficSignsCollectionDatabase? = null

        fun getDatabase(context: Context): TrafficSignsCollectionDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrafficSignsCollectionDatabase::class.java,
                    Data.TRAFFIC_DATABASE
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}