package com.example.trafficsigns.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.io.Serializable

@Entity(tableName = "my_profile")
data class MyProfile (
        @PrimaryKey(autoGenerate = false)
        var id: Int,
        var name: String? = "",
        var age: Int? = 0,
        var picturePath: String? = "",
        var address: String? = "",
        var phoneNumber: String? = "",
        var email: String? = "",
        var scores: ArrayList<Int>? = ArrayList(),
        var knownTrafficSigns: ArrayList<TrafficSign>? = ArrayList()
        ): Serializable {
}