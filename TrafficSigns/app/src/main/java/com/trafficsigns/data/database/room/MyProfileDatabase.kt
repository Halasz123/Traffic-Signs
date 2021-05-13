package com.trafficsigns.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trafficsigns.data.database.Convertes
import com.trafficsigns.data.database.dao.MyProfileDao
import com.trafficsigns.data.dataclass.MyProfile
import com.trafficsigns.ui.constant.Data

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
                        Data.PROFILE_DATABASE
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}