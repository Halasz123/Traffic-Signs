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
        var name: String? = null,
        var age: Int? = null,
        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        var picture: ByteArray? = null,
        var averageScore: Float? = null,
        var maxPoint: Int? = null,
        var knownTrafficSigns: List<TrafficSign>? = null
        ): Serializable {
}