package com.trafficsigns.data.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * Represents a table within the database. Room creates a table for each class that has
 * @Entity annotation, the fields in the class correspond to columns in the table.
 */
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