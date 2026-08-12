package com.leafree.backend.domain.device.service;

import com.leafree.backend.domain.crop.enums.CropStatus;
import com.leafree.backend.domain.crop.service.CropService;
import com.leafree.backend.domain.device.dto.DeviceRegistRequest;
import com.leafree.backend.domain.device.dto.DeviceRegistResponse;
import com.leafree.backend.domain.device.dto.UserDeviceResponse;
import com.leafree.backend.domain.device.entity.Device;
import com.leafree.backend.domain.device.entity.UserDevice;
import com.leafree.backend.domain.device.entity.UserDeviceId;
import com.leafree.backend.domain.device.repository.DeviceRepositroy;
import com.leafree.backend.domain.device.repository.UserDeviceRepository;
import com.leafree.backend.domain.user.dto.FcmTokenDto;
import com.leafree.backend.domain.user.entity.FcmToken;
import com.leafree.backend.domain.user.repository.FcmTokenRepository;
import com.leafree.backend.exception.ExceptionCode;
import com.leafree.backend.exception.type.DbException;
import com.leafree.backend.domain.crop.entity.Crop;
import com.leafree.backend.domain.crop.repository.CropRepository;
import com.leafree.backend.domain.device.dto.DeviceDetailResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepositroy deviceRepositroy;
    private final UserDeviceRepository userDeviceRepository;
    private final CropRepository cropRepository;
    private final CropService cropService;
    private final FcmTokenRepository fcmTokenRepository;

    @Override
    public DeviceRegistResponse addUserDevice(Long userId,DeviceRegistRequest request) {
        String deviceUuid = request.getDeviceUuid();
        Device device = deviceRepositroy.findByDeviceUuid(deviceUuid).orElseThrow(()->new DbException(ExceptionCode.DEVICE_NOT_FOUND));

        Long deviceId = device.getId();

        UserDevice newUserDevice = UserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .build();

        userDeviceRepository.save(newUserDevice);

        return DeviceRegistResponse.builder()
                .deviceId(deviceId)
                .deviceUuid(deviceUuid)
                .build();
    }

    @Override
    public Device createDevice(String deviceUuid) {
        Device newDevice = Device.builder()
                .deviceUuid(deviceUuid)
                .build();
        Device savedDevice = deviceRepositroy.save(newDevice);
        return savedDevice;
    }

    @Override
    public List<UserDeviceResponse> getAllUserDevice(Long userId) {
        List<UserDeviceResponse> list = userDeviceRepository.findByUserId(userId);
        return list;
    }

    @Override
    public void deleteUserDevice(Long userId, Long deviceId) {
        UserDeviceId userDeviceId = new UserDeviceId(userId, deviceId);
        userDeviceRepository.deleteById(userDeviceId);
    }

    @Override
    @Transactional
    public DeviceDetailResponseDto getDeviceDetail(Long deviceId) {
        Device device = deviceRepositroy.findById(deviceId)
                .orElseThrow(() -> new DbException(ExceptionCode.DEVICE_NOT_FOUND));

        // 현재 심어진 작물 조회
        Crop currentCrop = cropRepository.findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(deviceId, CropStatus.GROWING);

        if (currentCrop == null) {
            return DeviceDetailResponseDto.builder()
                    .deviceUuid(device.getDeviceUuid())
                    .build();
        }


        return DeviceDetailResponseDto.builder()
                .deviceUuid(device.getDeviceUuid())
                .cropId(currentCrop.getId())
                .cropType(currentCrop.getCropType())
                .cropStatus(String.valueOf(currentCrop.getStatus()))
                .createdAt(currentCrop.getCreatedAt())
                .harvestedAt(currentCrop.getHarvestedAt())
                .build();
    }

}
