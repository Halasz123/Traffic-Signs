package com.example.trafficsigns.ui.interfaces

import com.example.trafficsigns.data.TrafficSign

interface ItemClickListener {
    fun onItemClickListener(position: Int)
    fun onItemClickListener(trafficSign: TrafficSign)
    fun onItemLongClickListener(trafficSign: TrafficSign)
}