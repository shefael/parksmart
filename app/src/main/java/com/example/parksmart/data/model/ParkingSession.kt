package com.example.parksmart.data.model

import java.time.Duration
import java.time.Instant

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

data class ParkingSession(
    val id: String = "",
    val licensePlate: String = "",
    val vehicleType: VehicleType = VehicleType.VOITURE,
    val entryTime: Instant = Instant.now(),
    val exitTime: Instant? = null,
    val photoUrl: String? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val isPaid: Boolean = false,
    val totalAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Durée écoulée depuis l'entrée (ou jusqu'à la sortie si définie)
     */
    val currentDuration: Duration
        get() = if (exitTime != null) {
            Duration.between(entryTime, exitTime)
        } else {
            Duration.between(entryTime, Instant.now())
        }

    /**
     * Coût actuel basé sur la durée réelle
     * Arrondi à l'heure supérieure, 2 décimales
     */
    val currentCost: Double
        get() {
            val hours = kotlin.math.ceil(currentDuration.toMinutes().toDouble() / 60.0)
            return kotlin.math.round(hours * vehicleType.hourlyRate * 100) / 100
        }

    /**
     * Vrai si le véhicule dépasse 12h (alerte)
     */
    val isMaxDurationExceeded: Boolean
        get() = currentDuration.toHours() >= 12

    /**
     * Vrai si le véhicule est là depuis plus de 24h (oubli)
     */
    val isOverstayed: Boolean
        get() = currentDuration.toHours() >= 24
}