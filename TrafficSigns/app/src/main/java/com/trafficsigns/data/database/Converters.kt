package com.trafficsigns.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trafficsigns.data.dataclass.TrafficSign


/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * Converts objects to Json and back.
 * It is used before upload data to the database and after get data from database.
 */
class Convertes {

    @TypeConverter
    fun fromTrafficSignsToString(source: List<TrafficSign>) :String{
        return Gson().toJson(source)
    }

    @TypeConverter
    fun  fromStringToTrafficSigns(string: String): List<TrafficSign> {
        val type = object : TypeToken<List<TrafficSign>>() {}.type
        return Gson().fromJson(string, type)
    }

    @TypeConverter
    fun fromIntsToString(source: ArrayList<Int>): String{
        return Gson().toJson(source)
    }

    @TypeConverter
    fun  fromStringToInts(string: String): ArrayList<Int> {
        val type = object : TypeToken<ArrayList<Int>>() {}.type
        return Gson().fromJson(string, type)
    }
    @TypeConverter
    fun fromTrafficSignsToString2(source: ArrayList<TrafficSign>) :String{
        return Gson().toJson(source)
    }

    @TypeConverter
    fun  fromStringToTrafficSigns2(string: String): ArrayList<TrafficSign> {
        val type = object : TypeToken<ArrayList<TrafficSign>>() {}.type
        return Gson().fromJson(string, type)
    }


}