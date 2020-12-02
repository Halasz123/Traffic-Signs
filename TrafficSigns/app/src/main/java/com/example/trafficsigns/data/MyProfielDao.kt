package com.example.trafficsigns.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MyProfielDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addProfile(profile: MyProfile)

    @Update
    suspend fun updateProfile(profile: MyProfile)

    @Query("SELECT * FROM traffic_signs")
    fun getProfile(): LiveData<MyProfile>
}