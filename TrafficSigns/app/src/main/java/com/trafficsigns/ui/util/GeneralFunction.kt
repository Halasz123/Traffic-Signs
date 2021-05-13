package com.trafficsigns.ui.util

import com.google.gson.Gson
import com.trafficsigns.data.TrafficSign
import java.lang.reflect.Type


class GeneralFunction {

    companion object {
        val instance = GeneralFunction()
    }

    fun parseJson(json: String, type: Type): MutableMap<String, TrafficSign> {
        val gson = Gson()
        val list: MutableMap<String, TrafficSign> = gson.fromJson(json, type)
        return list
    }


}