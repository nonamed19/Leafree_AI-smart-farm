package com.leafree.app.di

import android.content.Context
import com.leafree.app.api.LeafreeApi
import com.leafree.app.network.AuthInterceptor
import com.leafree.app.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 20L

    @Volatile
    private var retrofit: Retrofit? = null
    @Volatile
    private var api: LeafreeApi? = null

    @Synchronized
    fun provideApi(context: Context): LeafreeApi {
        if (api != null) return api!!

        val tokenManager = TokenManager(context)
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.leafree.app.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(LeafreeApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit!!.create(LeafreeApi::class.java)
        return api!!
    }
}
