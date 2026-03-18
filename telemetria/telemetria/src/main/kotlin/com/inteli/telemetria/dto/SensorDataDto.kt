package com.inteli.telemetria.dto

data class SensorDataDTO(
    val sensorId: String,
    val temperatura: Double,
    val umidade: Double,
    val timestamp: String
)