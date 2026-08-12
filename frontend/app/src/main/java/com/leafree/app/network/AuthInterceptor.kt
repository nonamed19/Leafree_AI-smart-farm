package com.leafree.app.network

import com.leafree.app.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}
