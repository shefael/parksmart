package com.example.parksmart.di

import com.example.parksmart.data.remote.SupabaseApi
import com.example.parksmart.data.remote.SupabaseClient
import com.example.parksmart.data.storage.SupabaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ✅ Utilise le client EXPOSE de SupabaseClient (qui a déjà les headers)
    @Provides
    @Singleton
    fun provideOkHttpClient() = SupabaseClient.client

    // ✅ Utilise le retrofit EXPOSE de SupabaseClient
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = SupabaseClient.retrofit

    @Provides
    @Singleton
    fun provideSupabaseApi(retrofit: Retrofit): SupabaseApi {
        return retrofit.create(SupabaseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSupabaseStorage(
        @ApplicationContext context: android.content.Context
    ): SupabaseStorage {
        return SupabaseStorage(context)
    }
}