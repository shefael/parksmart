package com.example.parksmart.data.model

enum class VehicleType(val displayName: String, val hourlyRate: Double) {
    VOITURE("Voiture", 2.0),
    MOTO("Moto", 1.0),
    CAMION("Camion", 5.0);

    companion object {
        fun fromString(value: String): VehicleType {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                VOITURE
            }
        }
    }
}