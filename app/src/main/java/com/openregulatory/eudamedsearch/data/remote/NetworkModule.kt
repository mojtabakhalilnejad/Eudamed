package com.openregulatory.eudamedsearch.data.remote

import com.openregulatory.eudamedsearch.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Small, dependency-injection-free composition root for networking. Kept as plain singletons
 * (rather than Hilt/Koin) so the project has zero extra setup steps for whoever opens it.
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val contentType = "application/json".toMediaType()

    val siteApi: EudamedSiteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.EUDAMED_SITE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(EudamedSiteApi::class.java)
    }

    val officialApi: EudamedOfficialApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.EUDAMED_OFFICIAL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(EudamedOfficialApi::class.java)
    }
}
