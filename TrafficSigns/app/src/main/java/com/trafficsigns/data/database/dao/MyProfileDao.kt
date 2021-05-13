package com.trafficsigns.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.trafficsigns.data.dataclass.MyProfile

@Dao
interface MyProfileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addProfile(profile: MyProfile)

    @Update
    suspend fun updateProfile(profile: MyProfile)

    @Query("SELECT * FROM my_profile")
    fun getProfile(): LiveData<MyProfile>

}