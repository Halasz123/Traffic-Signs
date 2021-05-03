package com.example.trafficsigns.data

import java.io.Serializable

data class TrafficSign(
        var id: String? = null,
        var name: String? = null,
        var image: String? = null,
        var group: String? = null,
        var traffic_rules: String? = null,
        var description: String? = null
) : Serializable