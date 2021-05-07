package com.trafficsigns.ui.network.utils

import com.trafficsigns.data.TrafficSign

class TrafficSignMemoryCache {

    private var trafficList: ArrayList<TrafficSign> = ArrayList()

    companion object {
        val instance = TrafficSignMemoryCache()
    }

    fun cacheTrafficSign(trafficSign: TrafficSign) {
        trafficList.add(trafficSign)
    }

    fun getCachedTrafficSign(id: String): TrafficSign? {
        return (trafficList.firstOrNull { it.id == id} )
    }



}