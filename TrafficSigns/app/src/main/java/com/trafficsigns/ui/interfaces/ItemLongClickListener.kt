package com.trafficsigns.ui.interfaces

import com.trafficsigns.data.dataclass.TrafficSign

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * Contains the methods for the long click listener.
 */
interface ItemLongClickListener {
    fun onItemLongClickListener(trafficSign: TrafficSign)
}