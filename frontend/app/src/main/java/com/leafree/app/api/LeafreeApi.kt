package com.leafree.app.api

import com.leafree.app.model.CropInfo
import com.leafree.app.model.EventResponse
import com.leafree.app.model.SettingInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Leafree 백엔드 서버와의 REST API 통신을 위한 인터페이스
 * 모든 API 엔드포인트는 https://leafree.kr/api를 기본 URL로 사용
 * 기기는 deviceId = 1 하나라 일단 mvp에서는 하드코딩
 */
interface LeafreeApi {
    companion object {
        const val BASE_URL = "https://leafree.kr/api/"
    }

    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): LoginResponse

    data class LoginRequest(
        val email: String,
        val password: String
    )

    data class LoginResponse(
        val userId: String,
        val accessToken: String
    )

    @GET("device/{deviceId}")
    suspend fun getCropInfo(@Path("deviceId") deviceId: Long): CropInfo

    @GET("device/{deviceId}/image")
    suspend fun getCropImage(@Path("deviceId") deviceId: Long): ResponseBody

    @GET("device/{deviceId}/setting")
    suspend fun getSettings(@Path("deviceId") deviceId: Long): SettingInfo

    @PATCH("device/{deviceId}/setting")
    suspend fun updateSetting(
        @Path("deviceId") deviceId: Long,
        @Body setting: SettingInfo
    )

    @GET("device/{deviceId}/event")
    suspend fun getEvents(@Path("deviceId") deviceId: Long, @Query("date") date: String): List<EventResponse>

    @POST("device/{deviceId}/fcm-token")
    suspend fun sendFcmToken(
        @Path("deviceId") deviceId: Long,
        @Body token: FcmTokenRequest
    ): Response<Unit>

    data class FcmTokenRequest(
        val fcmToken: String
    )

}
