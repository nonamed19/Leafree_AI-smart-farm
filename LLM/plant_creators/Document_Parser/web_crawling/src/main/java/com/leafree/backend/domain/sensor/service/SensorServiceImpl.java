package com.leafree.backend.domain.sensor.service;

import com.leafree.backend.domain.sensor.entity.Sensor;
import com.leafree.backend.domain.sensor.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public Sensor saveSensorData(String deviceId, String content, float value) {
        Sensor.SensorBuilder builder = Sensor.builder()
                .deviceId(Long.parseLong(deviceId));

        // 센서 타입에 따라 해당 필드 설정
        switch (content) {
            case "temperature" -> builder.temperature(value);
            case "humidity" -> builder.humidity((int) value);
            case "light" -> builder.light((int) value);
            case "co2_level" -> builder.co2Level((int) value);
            case "water_level" -> builder.waterLevel((int) value);
            case "soil_moisture" -> builder.soilMoisture((int) value);
            default -> throw new IllegalArgumentException("Invalid sensor type: " + content);
        }

        return sensorRepository.save(builder.build());
    }

    @Override
    @Transactional
    public Sensor saveSensorData(Sensor sensor) {
        // 센서 타입에 따라 해당 필드 설정
        Sensor.SensorBuilder builder = Sensor.builder()
            .deviceId(sensor.getDeviceId())
            .temperature(sensor.getTemperature())
            .humidity(sensor.getHumidity())
            .light(sensor.getLight())
            .co2Level(sensor.getCo2Level())
            .waterLevel(sensor.getWaterLevel())
            .soilMoisture(sensor.getSoilMoisture());

        return sensorRepository.save(builder.build());
    }
}
