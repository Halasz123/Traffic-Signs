package com.trafficsigns.data.database.repository

import androidx.lifecycle.LiveData
import com.trafficsigns.data.database.dao.MyProfileDao
import com.trafficsigns.data.dataclass.MyProfile

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * A repository class abstracts access to multiple data sources from MyProfileDatabase.
 * It contains LiveData to keep track of changes in data.
 */
class MyProfileRepository(private val myProfileDao: MyProfileDao) {

    val getMyProfile: LiveData<MyProfile> =  myProfileDao.getProfile()


    suspend fun addProfile(profile: MyProfile){
        myProfileDao.addProfile(profile)
    }

    suspend fun updateProfile(profile: MyProfile) {
        myProfileDao.updateProfile(profile)
    }
}