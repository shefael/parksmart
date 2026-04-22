package com.example.parksmart.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.parksmart.MainActivity
import com.example.parksmart.ParkSmartApplication
import com.example.parksmart.R
import com.example.parksmart.data.remote.SupabaseApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.Instant

@HiltWorker
class ParkingAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: SupabaseApi
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val response = api.getActiveSessions()
            if (response.isSuccessful) {
                val sessions = response.body() ?: emptyList()
                val now = Instant.now()

                sessions.forEach { dto ->
                    dto.entry_time_epoch?.let { epoch ->
                        val entryTime = Instant.ofEpochMilli(epoch)
                        val duration = Duration.between(entryTime, now)

                        if (duration.toHours() >= 24) {
                            sendNotification(
                                "Véhicule oublié !",
                                "La plaque ${dto.license_plate} est stationnée depuis plus de 24h.",
                                dto.id.hashCode()
                            )
                        } else if (duration.toHours() >= 12) {
                            sendNotification(
                                "Durée maximale dépassée",
                                "La plaque ${dto.license_plate} dépasse les 12h de stationnement.",
                                dto.id.hashCode()
                            )
                        }
                    }
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            ParkSmartApplication.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
