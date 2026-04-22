package com.example.parksmart.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    // ✅ TES CLÉS (déjà correctes)
    const val SUPABASE_URL = "https://mhixjwlmzxmvvxhxhhlq.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1oaXhqd2xtenhtdnZ4aHhoaGxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY2OTcxNzgsImV4cCI6MjA5MjI3MzE3OH0.tE1-VP2vDDAm6EHyNkhTSTNmEeMdmOFWdC-9uEFhH1Y"
    const val BUCKET_NAME = "parking-photos"

    // Interceptor qui ajoute automatiquement les headers Supabase à CHAQUE requête
    private val supabaseInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("apikey", SUPABASE_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_KEY")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Client HTTP partagé avec les headers Supabase
    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(supabaseInterceptor) // ← AJOUTÉ : headers pour TOUTES les requêtes
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("$SUPABASE_URL/rest/v1/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}