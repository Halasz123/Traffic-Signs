package com.example.trafficsigns.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyProfile::class], version = 1, exportSchema = false)
@TypeConverters(Convertes::class)
abstract  class MyProfileDatabase: RoomDatabase() {

    abstract fun myProfileDao(): MyProfileDao

    companion object{
        @Volatile
        private var INSTANCE: MyProfileDatabase? = null

        fun getDatabase(context: Context): MyProfileDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        MyProfileDatabase::class.java,
                        "my_profile_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}