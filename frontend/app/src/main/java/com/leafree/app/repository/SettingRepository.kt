package com.leafree.app.repository

import android.content.Context
import com.leafree.app.api.LeafreeApi
import com.leafree.app.di.NetworkModule
import com.leafree.app.model.SettingInfo

/**
 * 설정 관련 데이터를 관리하는 Repository 클래스
 * 백엔드 API를 통해 설정 정보를 조회하고 업데이트하는 기능 제공
 */
class SettingRepository(private val context: Context) {
    private val api: LeafreeApi = NetworkModule.provideApi(context)

    /**
     * 특정 기기의 설정 정보를 조회
     * @param deviceId 기기 ID
     * @return 설정 정보
     */
    suspend fun getSettings(deviceId: Long): SettingInfo {
        return api.getSettings(deviceId)
    }

    /**
     * 특정 기기의 설정 정보를 업데이트
     * @param deviceId 기기 ID
     * @param setting 업데이트할 설정 정보
     */
    suspend fun updateSetting(deviceId: Long, setting: SettingInfo) {
        api.updateSetting(deviceId, setting)
    }
}
