package com.trafficsigns.data.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trafficsigns.data.dataclass.TrafficSign
import java.io.Serializable

@Entity(tableName = "traffic_signs")
data class TrafficSignsCollection (
    @PrimaryKey(autoGenerate = false)
    val groupId: String,
    val trafficSigns: List<TrafficSign>
): Serializable