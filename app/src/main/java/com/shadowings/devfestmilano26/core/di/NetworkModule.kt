package com.shadowings.devfestmilano26.core.di

import com.shadowings.devfestmilano26.BuildConfig
import com.shadowings.devfestmilano26.apod.data.remote.ApodApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

private const val NASA_BASE_URL = "https://api.nasa.gov/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("nasaApodApiKey")
    fun provideNasaApodApiKey(): String = BuildConfig.NASA_APOD_API_KEY

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                // Redact api_key so it never reaches Logcat, even in debug builds
                // (secure-storage-audit finding; constitution Principle II).
                val redactingLogger = HttpLoggingInterceptor.Logger { message ->
                    Log.d("ApodHttp", message.replace(Regex("api_key=[^&\\s]+"), "api_key=REDACTED"))
                }
                addInterceptor(HttpLoggingInterceptor(redactingLogger).apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(NASA_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideApodApiService(retrofit: Retrofit): ApodApiService =
        retrofit.create(ApodApiService::class.java)
}
