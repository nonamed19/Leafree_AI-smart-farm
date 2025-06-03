package com.leafree.backend.domain.setting.service;


import com.leafree.backend.domain.setting.dto.SettingResponse;
import com.leafree.backend.domain.setting.dto.UpdateSettingRequest;
import com.leafree.backend.domain.setting.dto.UpdateSettingResponse;

public interface SettingService {

    UpdateSettingResponse updateSetting(Long deviceId, UpdateSettingRequest updateSettingRequest);
    SettingResponse getSetting(Long deviceId);
}
