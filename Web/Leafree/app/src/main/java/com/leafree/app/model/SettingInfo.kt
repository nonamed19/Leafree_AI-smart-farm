package com.leafree.app.model

data class SettingInfo(
    val temperature: Double,
    val humidity: Double,
    val light: Double,
    val co2Ppm: Double,
    val soilHumidity: Double,
    val lightDuration: Double,
    val wateringInterval: Double
)