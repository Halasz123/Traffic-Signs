package com.trafficsigns.ui.network.utils

import com.trafficsigns.data.TrafficSign

/**
 * @author: Halász Botond
 * @since: 10/05/2021
 *
 * Cache memory for the specified list what the neural network can recognize.
 */
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