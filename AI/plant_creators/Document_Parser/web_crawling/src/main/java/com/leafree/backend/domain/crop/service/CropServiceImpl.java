package com.leafree.backend.domain.crop.service;

import com.leafree.backend.domain.crop.dto.AddCropRequest;
import com.leafree.backend.domain.crop.dto.CropEnvironmentDto;
import com.leafree.backend.domain.crop.dto.CropEnvironmentWrapperDto;
import com.leafree.backend.domain.crop.entity.Crop;
import com.leafree.backend.domain.crop.enums.CropStatus;
import com.leafree.backend.domain.crop.repository.CropRepository;
import com.leafree.backend.domain.device.repository.DeviceRepositroy;
import com.leafree.backend.domain.event.enums.EventContentCode;
import com.leafree.backend.domain.event.service.EventService;
import com.leafree.backend.domain.setting.dto.UpdateSettingRequest;
import com.leafree.backend.domain.setting.service.SettingService;
import com.leafree.backend.exception.ExceptionCode;
import com.leafree.backend.exception.type.DbException;
import com.leafree.backend.mqtt.MqttPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CropServiceImpl implements CropService {

    private final CropRepository cropRepository;
    private final CropInfoClient cropInfoClient;
    private final SettingService settingService;
    private final EventService eventService;
    private final DeviceRepositroy deviceRepositroy;
    private final MqttPublisher mqttPublisher;

    @Override
    @Transactional
    public void addCrop(AddCropRequest request) {
        Long deviceId = request.getDeviceId();
        String requestCropType = request.getCropType();

        if (!deviceRepositroy.findById(deviceId).isPresent()) {
            throw new DbException(ExceptionCode.DEVICE_NOT_FOUND);
        }

        CropEnvironmentWrapperDto wrapperDto =  cropInfoClient.getCropInfo(requestCropType);

        if (wrapperDto == null || wrapperDto.getEnvironment() == null) {
            log.warn("작물 환경 정보를 가져오지 못했습니다. 작물 등록 중단. deviceId={}, cropType={}", deviceId, requestCropType);
            return;
        }

        String cropType = wrapperDto.getCropType();
        CropEnvironmentDto rawEnvironment  = wrapperDto.getEnvironment();

        byte[] cropImage = cropInfoClient.getCropImage(cropType);

        if (cropImage == null) {
            log.warn("작물 이미지를 가져오지 못했습니다.");
            return;
        }

        mqttPublisher.publishImageMessage(deviceId, cropImage);

        Float harvestTime = "N/A".equals(rawEnvironment.getHarvestTime()) ? 0 : Float.parseFloat(rawEnvironment.getHarvestTime());

        LocalDate harvestDate = (harvestTime != null && harvestTime > 0)
                ? LocalDate.now().plusDays(harvestTime.longValue())
                : null;

        Crop newCrop = Crop.builder()
                .deviceId(deviceId)
                .cropType(cropType)
                .cropImage(cropImage)
                .status(CropStatus.GROWING)
                .harvestedAt(harvestDate)
                .build();

        cropRepository.save(newCrop);

        UpdateSettingRequest updateSettingRequest = UpdateSettingRequest.builder()
                .temperature(Float.valueOf(rawEnvironment.getAirTemperature()))
                .humidity(Integer.valueOf(rawEnvironment.getAirHumidity()))
                .light(Integer.valueOf(rawEnvironment.getQuantumLux()))
                .co2Ppm(Integer.valueOf(rawEnvironment.getCo2Level()))
                .wateringInterval(Integer.valueOf(rawEnvironment.getIrrigationInterval()))
                .lightDuration(Integer.valueOf(rawEnvironment.getPhotoperiod()))
                .soilHumidity(Integer.valueOf(rawEnvironment.getHighSoilHumi()))
                .build();

        settingService.updateSetting(deviceId,updateSettingRequest);

        eventService.logNotifyEvent(deviceId, EventContentCode.CROP_REGISTERED, cropType);

    }

    @Override
    @Transactional
    public void deleteCrop(Long deviceId, Long cropId) {
        Crop crop = cropRepository.findById(cropId).orElseThrow(() -> new DbException(ExceptionCode.CROP_NOT_FOUND));
        crop.deleteCrop();
        eventService.logNotifyEvent(deviceId, EventContentCode.CROP_DELETED,crop.getCropType());
    }

    @Override
    public byte[] getLatestCropImage(Long deviceId) {
        Crop crop = cropRepository.findTopByDeviceIdAndStatusOrderByCreatedAtDesc(deviceId,CropStatus.GROWING)
                .orElseThrow(()-> new DbException(ExceptionCode.CROP_NOT_FOUND));
        return crop.getCropImage();
    }
}
