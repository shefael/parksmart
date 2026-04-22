package com.example.parksmart.data.storage

import android.content.Context
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Encode une image en Base64 au lieu de l'uploader vers Supabase Storage.
     * Retourne une data URI : "data:image/jpeg;base64,/9j/4AAQ..."
     */
    suspend fun encodeImageToBase64(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open URI"))

            // Lire l'image et la compresser si trop grande
            val originalBytes = inputStream.use { it.readBytes() }

            // Détecter le type MIME
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

            // Encoder en Base64
            val base64String = Base64.encodeToString(originalBytes, Base64.NO_WRAP)

            // Construire la data URI
            val dataUri = "data:$mimeType;base64,$base64String"

            Result.success(dataUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}