package com.trafficsigns.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trafficsigns.ui.constant.Data

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