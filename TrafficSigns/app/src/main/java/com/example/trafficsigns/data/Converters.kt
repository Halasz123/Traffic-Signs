package com.example.trafficsigns.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

class Convertes {

    @TypeConverter
    fun fromBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    @TypeConverter
    fun fromTrafficSignsCollectionToString(source: TrafficSignsCollection) :String{
        return Gson().toJson(source)
    }

    @TypeConverter
    fun  fromStringToTrafficSignsCollection(string: String): TrafficSignsCollection {
        return Gson().fromJson(string, TrafficSignsCollection::class.java)
    }

    @TypeConverter
    fun fromTrafficSignToString(source: TrafficSign) :String{
        return Gson().toJson(source)
    }

    @TypeConverter
    fun  fromStringToTrafficSign(string: String): TrafficSign {
        return Gson().fromJson(string, TrafficSign::class.java)
    }

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