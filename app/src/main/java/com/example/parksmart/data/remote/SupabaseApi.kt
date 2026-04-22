package com.example.parksmart.data.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApi {

    @GET("parking_sessions")
    suspend fun getActiveSessions(
        @Query("status") status: String = "eq.ACTIVE",
        @Query("order") order: String = "entry_time_epoch.desc"
    ): Response<List<ParkingSessionDto>>

    @GET("parking_sessions")
    suspend fun getAllSessions(
        @Query("order") order: String = "created_at.desc"
    ): Response<List<ParkingSessionDto>>

    @POST("parking_sessions")
    @Headers("Prefer: return=representation")
    suspend fun createSession(
        @Body session: JsonObject
    ): Response<List<ParkingSessionDto>>

    @PATCH("parking_sessions")
    @Headers("Prefer: return=representation")
    suspend fun updateSession(
        @Query("id") id: String,
        @Body updates: JsonObject
    ): Response<List<ParkingSessionDto>>

    @DELETE("parking_sessions")
    suspend fun deleteSession(
        @Query("id") id: String
    ): Response<Unit>
}

// ✅ TOUT en String pour éviter les NumberFormatException
// On parse manuellement après coup avec OffsetDateTime
data class ParkingSessionDto(
    val id: String,
    val license_plate: String,
    val vehicle_type: String,
    val entry_time_epoch: Long?,           // Peut être null si pas encore migré
    val exit_time_epoch: Long?,
    val entry_time: String?,                // ISO string au cas où
    val exit_time: String?,
    val photo_url: String?,
    val is_paid: Boolean,
    val total_amount: Double,
    val status: String,
    val created_at: String?                 // ✅ String au lieu de Long !
)