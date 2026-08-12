package com.leafree.app.repository

import android.content.Context
import android.util.Log
import com.leafree.app.api.LeafreeApi
import com.leafree.app.di.NetworkModule
import com.leafree.app.model.CropInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * 작물 관련 데이터를 관리하는 Repository 클래스
 * 백엔드 API를 통해 작물 정보를 조회하는 기능 제공
 */
class CropRepository(
    private val context: Context,
    private val cropImageRepo: CropImageRepository
) {
    private val api: LeafreeApi = NetworkModule.provideApi(context)

    suspend fun getCropInfo(deviceId: Long): CropInfo {
        Log.d("CropRepository", "요청 시작됨")
        val (crop, imageBytes) = coroutineScope {
            val cropRequest = async { api.getCropInfo(deviceId) }
            val imageRequest = async { api.getCropImage(deviceId).bytes() }
            cropRequest.await() to imageRequest.await()
        }
        val imageUri = cropImageRepo.saveImageToInternalStorage(context, imageBytes)

        Log.d("CropRepository", "요청 완료: $crop")
        return crop.copy(cropImage = imageUri.toString())
    }
}
