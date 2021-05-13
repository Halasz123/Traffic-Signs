package com.trafficsigns.data.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.trafficsigns.data.database.room.MyProfileDatabase
import com.trafficsigns.data.database.repository.MyProfileRepository
import com.trafficsigns.data.dataclass.MyProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyProfileViewModel(application: Application): AndroidViewModel(application) {

    val myProfile: LiveData<MyProfile>
    private val repository: MyProfileRepository

    init {
        val myProfileDao = MyProfileDatabase.getDatabase(application).myProfileDao()
        repository = MyProfileRepository(myProfileDao)
        myProfile = repository.getMyProfile
    }

    fun addMyProfile(profile: MyProfile){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addProfile( profile)
        }
    }

    fun updateProfile(profile: MyProfile){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(profile)
        }
    }
}