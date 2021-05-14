package com.trafficsigns.data.dataclass

import java.io.Serializable

/**
 * @author Halász Botond
 * @since 14/05/2021
 *
 * It is a base data class what contains the main properties of a Traffic Sign.
 */
data class TrafficSign(
        var id: String? = null,
        var name: String? = null,
        var image: String? = null,
        var group: String? = null,
        var traffic_rules: String? = null,
        var description: String? = null
) : Serializable