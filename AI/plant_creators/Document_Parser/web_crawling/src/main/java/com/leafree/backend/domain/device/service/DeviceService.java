package com.leafree.backend.domain.device.service;

import com.leafree.backend.domain.device.dto.DeviceRegistRequest;
import com.leafree.backend.domain.device.dto.DeviceRegistResponse;
import com.leafree.backend.domain.device.dto.UserDeviceResponse;
import com.leafree.backend.domain.device.dto.DeviceDetailResponseDto;
import com.leafree.backend.domain.device.entity.Device;
import com.leafree.backend.domain.user.dto.FcmTokenDto;

import java.util.List;

public interface DeviceService {
    DeviceRegistResponse addUserDevice(Long userId, DeviceRegistRequest request);

    Device createDevice(String deviceUuid);

    List<UserDeviceResponse> getAllUserDevice(Long userId);

    boolean hasDeviceAccess(Long userId, Long deviceId);

    void deleteUserDevice(Long userId, Long deviceId);

    DeviceDetailResponseDto getDeviceDetail(Long deviceId);
}
