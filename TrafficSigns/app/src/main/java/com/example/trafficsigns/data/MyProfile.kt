package com.example.trafficsigns.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.io.Serializable

@Entity(tableName = "my_profile")
data class MyProfile (
        @PrimaryKey(autoGenerate = false)
        var name: String? = null,
        var age: Int? = null,
        var picture: File? = null,
        var averageScore: Float? = null,
        var maxPoint: Int? = null,
        var knownTrafficSigns: List<TrafficSign>? = null
        ): Serializable