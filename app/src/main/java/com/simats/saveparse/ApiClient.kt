package com.simats.saveparse

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://pmgjc2px-80.inc1.devtunnels.ms/savepaws/"
    const val IMAGE_BASE_URL = "https://pmgjc2px-80.inc1.devtunnels.ms/savepaws/"

    // OkHttpClient with extended timeouts for image uploads
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Connection timeout
            .readTimeout(60, TimeUnit.SECONDS)     // Read timeout
            .writeTimeout(60, TimeUnit.SECONDS)    // Write timeout (important for uploads)
            .build()
    }

    // 🔹 Expose Retrofit instance (optional but useful)
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Use custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🔹 Existing usage remains unchanged
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

