package com.leafree.app.model

data class SensorLogDto(
    val temperature: Float,
    val humidity: Int,
    val light: Int,
    val co2Ppm: Int,
    val waterLevel: Int,
    val soilMoisture: Int
)

