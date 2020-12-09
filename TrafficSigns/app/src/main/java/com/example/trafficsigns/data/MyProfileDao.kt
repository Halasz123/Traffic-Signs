package com.example.trafficsigns.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import java.util.concurrent.Flow

@Dao
interface MyProfileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addProfile(profile: MyProfile)

    @Update
    suspend fun updateProfile(profile: MyProfile)

    @Query("SELECT * FROM my_profile")
    fun getProfile(): LiveData<MyProfile>

}