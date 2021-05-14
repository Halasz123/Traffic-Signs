package com.trafficsigns.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.trafficsigns.data.dataclass.MyProfile

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * It is an interface of MyProfileDatabase and his queries.
 * Contains the methods used for accessing the database.
 */
@Dao
interface MyProfileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addProfile(profile: MyProfile)

    @Update
    suspend fun updateProfile(profile: MyProfile)

    @Query("SELECT * FROM my_profile")
    fun getProfile(): LiveData<MyProfile>

}