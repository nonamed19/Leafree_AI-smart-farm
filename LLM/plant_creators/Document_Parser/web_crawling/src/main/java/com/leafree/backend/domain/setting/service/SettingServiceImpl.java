package com.leafree.backend.domain.setting.service;

import com.leafree.backend.domain.event.enums.EventContentCode;
import com.leafree.backend.domain.event.service.EventService;
import com.leafree.backend.domain.setting.dto.SettingResponse;
import com.leafree.backend.domain.setting.dto.UpdateSettingRequest;
import com.leafree.backend.domain.setting.dto.UpdateSettingResponse;
import com.leafree.backend.domain.setting.entity.Setting;
import com.leafree.backend.domain.setting.repository.SettingRepository;
import com.leafree.backend.exception.ExceptionCode;
import com.leafree.backend.exception.type.DbException;
import com.leafree.backend.mqtt.MqttPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingRepository settingRepository;
    private final MqttPublisher mqttPublisher;
    private final EventService eventService;

    @Override
    @Transactional
    public UpdateSettingResponse updateSetting(Long deviceId, UpdateSettingRequest updateSettingRequest) {
        Optional<Setting> existingSetting = settingRepository.findById(deviceId);
        Setting setting;

        if (existingSetting.isPresent()) {
            log.info("Setting exists with id: {}", deviceId);
            setting = existingSetting.get();
            updateExistingSetting(deviceId, setting, updateSettingRequest);
            setting = settingRepository.save(setting);
        } else {
            log.info("Setting not found with id: {}", deviceId);
            setting = createNewSetting(deviceId, updateSettingRequest);
            setting = settingRepository.save(setting);
        }

        if(setting!=null) {
            publishMqttMessages(deviceId, setting);
        }

        return buildResponse(setting);

    }

    private void updateExistingSetting(Long deviceId, Setting setting, UpdateSettingRequest request){
        if(request.getTemperature() != null) {
            Float oldValue = setting.getOptimalTemperature();
            setting.setOptimalTemperature(request.getTemperature());

            if (!request.getTemperature().equals(oldValue)) {
                eventService.logActuatingEvent(deviceId, EventContentCode.TEMP_CHANGED, oldValue, request.getTemperature());
            }
        }

        if(request.getHumidity() != null) {
            Integer oldValue = setting.getOptimalHumidity();
            setting.setOptimalHumidity(request.getHumidity());

            if (!request.getHumidity().equals(oldValue)) {
                eventService.logActuatingEvent(deviceId, EventContentCode.HUM_CHANGED, oldValue, request.getHumidity());
            }
        }

        if(request.getLight() != null) {
            Integer oldValue = setting.getOptimalLight();
            setting.setOptimalLight(request.getLight());

            if (!request.getLight().equals(oldValue)) {
                eventService.logActuatingEvent(deviceId, EventContentCode.LIGHT_CHANGED, oldValue, request.getLight());
            }
        }

        if(request.getLightDuration() != null) {
            Integer oldValue = setting.getLightDuration();
            setting.setLightDuration(request.getLightDuration());

            if (!request.getLightDuration().equals(oldValue)) {
                eventService.logActuatingEvent(deviceId, EventContentCode.LIGHT_DURATION_CHANGED, oldValue, request.getLightDuration());
            }
        }

        if(request.getWateringInterval() != null) {
            Integer oldValue = setting.getWateringInterval();
            setting.setWateringInterval(request.getWateringInterval());

            if (!request.getWateringInterval().equals(oldValue)) {
                eventService.logActuatingEvent(deviceId, EventContentCode.WATERING_INTERVAL_CHANGED, oldValue, request.getWateringInterval());
            }
        }

        if(request.getCo2Ppm() != null) {
            Integer oldValue = setting.getOptimalCo2Ppm();
            setting.setOptimalCo2Ppm(request.getCo2Ppm());

            if (!request.getCo2Ppm().equals(oldValue)) {
                eventService.logActuatingEvent(deviceId, EventContentCode.CO2_PPM_CHANGED, oldValue, request.getCo2Ppm());
            }
        }

        if(request.getSoilHumidity() != null) {
            Integer oldValue = setting.getOptimalHumidity();
            setting.setOptimalSoilHumidity(request.getSoilHumidity());

            if (!request.getSoilHumidity().equals(oldValue)) {
                eventService.logActuatingEvent(deviceId, EventContentCode.SOIL_HUMI_CHANGED, oldValue, request.getSoilHumidity());
            }
        }
    }

    private Setting createNewSetting(Long deviceId, UpdateSettingRequest request) {
        return Setting.builder()
                .deviceId(deviceId)
                .optimalTemperature(request.getTemperature())
                .optimalHumidity(request.getHumidity())
                .optimalLight(request.getLight())
                .lightDuration(request.getLightDuration())
                .wateringInterval(request.getWateringInterval())
                .optimalCo2Ppm(request.getCo2Ppm())
                .optimalSoilHumidity(request.getSoilHumidity())
                .build();
    }

    private void publishMqttMessages(Long deviceId, Setting setting) {
        if (setting.getOptimalTemperature() != null) {
            mqttPublisher.publishMessage(deviceId, "temperature", setting.getOptimalTemperature().toString());
        }

        if (setting.getOptimalHumidity() != null) {
            mqttPublisher.publishMessage(deviceId, "humidity", setting.getOptimalHumidity().toString());
        }

        if (setting.getOptimalLight() != null) {
            mqttPublisher.publishMessage(deviceId, "light", setting.getOptimalLight().toString());
        }

        if (setting.getLightDuration() != null) {
            mqttPublisher.publishMessage(deviceId, "light_duration", setting.getLightDuration().toString());
        }

        if (setting.getWateringInterval() != null) {
            mqttPublisher.publishMessage(deviceId,"watering_interval", setting.getWateringInterval().toString());
        }

        if (setting.getOptimalCo2Ppm() != null) {
            mqttPublisher.publishMessage(deviceId,"co2_ppm", setting.getOptimalCo2Ppm().toString());
        }

        if(setting.getOptimalSoilHumidity() != null) {
            mqttPublisher.publishMessage(deviceId,"soil_moisture", setting.getOptimalSoilHumidity().toString());
        }
    }

    private UpdateSettingResponse buildResponse(Setting setting) {
        return UpdateSettingResponse.builder()
                .temperature(setting.getOptimalTemperature())
                .humidity(setting.getOptimalHumidity())
                .light(setting.getOptimalLight())
                .co2Ppm(setting.getOptimalCo2Ppm())
                .lightDuration(setting.getLightDuration())
                .waterInterval(setting.getWateringInterval())
                .updateAt(setting.getUpdatedAt())
                .soilHumidity(setting.getOptimalSoilHumidity())
                .build();
    }

    @Override
    public SettingResponse getSetting(Long deviceId) {

        Setting setting = settingRepository.findById(deviceId).orElseThrow(() -> new DbException(ExceptionCode.DEVICE_NOT_FOUND));

        return SettingResponse.builder()
                .temperature(setting.getOptimalTemperature())
                .humidity(setting.getOptimalHumidity())
                .light(setting.getOptimalLight())
                .co2Ppm(setting.getOptimalCo2Ppm())
                .lightDuration(setting.getLightDuration())
                .wateringInterval(setting.getWateringInterval())
                .soilHumidity(setting.getOptimalSoilHumidity())
                .updateAt(setting.getUpdatedAt())
                .build();
    }
}
