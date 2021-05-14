package com.trafficsigns.ui.singleton

import com.google.gson.Gson
import com.trafficsigns.data.dataclass.TrafficSign
import java.lang.reflect.Type

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * Contains the methods and values what generally can be used anywhere in project.
 */
class GeneralSingleton {

    companion object{
        var isGrid = false
        val instance = GeneralSingleton()
    }

    fun parseJson(json: String, type: Type): MutableMap<String, TrafficSign> {
        val gson = Gson()
        val list: MutableMap<String, TrafficSign> = gson.fromJson(json, type)
        return list
    }


}