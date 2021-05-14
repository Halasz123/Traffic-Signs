package com.trafficsigns.data.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trafficsigns.data.dataclass.TrafficSign
import java.io.Serializable

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * Represents a table within the database. Room creates a table for each class that has
 * @Entity annotation, the fields in the class correspond to columns in the table.
 */
@Entity(tableName = "traffic_signs")
data class TrafficSignsCollection (
    @PrimaryKey(autoGenerate = false)
    val groupId: String,
    val trafficSigns: List<TrafficSign>
): Serializable