package com.example.parksmart.data.repository

import com.example.parksmart.data.model.ParkingSession
import com.example.parksmart.data.model.SessionStatus
import com.example.parksmart.data.model.VehicleType
import com.example.parksmart.data.remote.ParkingSessionDto
import com.example.parksmart.data.remote.SupabaseApi
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingRepository @Inject constructor(
    private val api: SupabaseApi
) {

    fun getActiveSessions(): Flow<Result<List<ParkingSession>>> = flow {
        try {
            val response = api.getActiveSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                emit(Result.success(sessions))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Erreur ${response.code()}"
                emit(Result.failure(Exception(errorBody)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getAllSessions(): Flow<Result<List<ParkingSession>>> = flow {
        try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                emit(Result.success(sessions))
            } else {
                emit(Result.failure(Exception("Erreur ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun createSession(session: ParkingSession): Result<ParkingSession> {
        return try {
            val json = JsonObject().apply {
                addProperty("id", session.id)
                addProperty("license_plate", session.licensePlate)
                addProperty("vehicle_type", session.vehicleType.name)
                // ✅ Envoi de l'epoch en millisecondes (UTC)
                addProperty("entry_time_epoch", session.entryTime.toEpochMilli())
                addProperty("photo_url", session.photoUrl)
                addProperty("status", session.status.name)
                addProperty("is_paid", false)
                addProperty("total_amount", 0.0)
            }

            val response = api.createSession(json)

            if (response.isSuccessful) {
                val created = response.body()?.firstOrNull()?.toDomain() ?: session.copy()
                Result.success(created)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Erreur ${response.code()}"
                Result.failure(Exception("Erreur création: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeSession(id: String, exitTime: Instant, amount: Double): Result<ParkingSession> {
        return try {
            val json = JsonObject().apply {
                addProperty("exit_time_epoch", exitTime.toEpochMilli())
                addProperty("total_amount", amount)
                addProperty("is_paid", true)
                addProperty("status", SessionStatus.COMPLETED.name)
            }

            val response = api.updateSession("eq.$id", json)
            if (response.isSuccessful) {
                Result.success(response.body()?.firstOrNull()?.toDomain() ?: ParkingSession())
            } else {
                Result.failure(Exception("Erreur mise à jour: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelSession(id: String): Result<Unit> {
        return try {
            val json = JsonObject().apply {
                addProperty("status", SessionStatus.CANCELLED.name)
            }
            val response = api.updateSession("eq.$id", json)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur annulation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSession(id: String): Result<Unit> {
        return try {
            val response = api.deleteSession("eq.$id")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur suppression: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ParkingSessionDto.toDomain(): ParkingSession {
        return ParkingSession(
            id = id,
            licensePlate = license_plate,
            vehicleType = VehicleType.fromString(vehicle_type),
            entryTime = entry_time_epoch?.let { Instant.ofEpochMilli(it) } ?: Instant.now(),
            exitTime = exit_time_epoch?.let { Instant.ofEpochMilli(it) },
            photoUrl = photo_url,
            isPaid = is_paid,
            totalAmount = total_amount,
            status = try { SessionStatus.valueOf(status) } catch (e: Exception) { SessionStatus.ACTIVE },
            createdAt = created_at?.let {
                try {
                    OffsetDateTime.parse(it).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis()
        )
    }
}
