package com.leafree.app.di

import android.content.Context
import com.leafree.app.api.LeafreeApi
import com.leafree.app.network.AuthInterceptor
import com.leafree.app.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private var retrofit: Retrofit? = null
    private var api: LeafreeApi? = null

    fun provideApi(context: Context): LeafreeApi {
        if (api != null) return api!!

        val tokenManager = TokenManager(context)
        val logging = HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY 
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
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