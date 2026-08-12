package com.leafree.backend.domain.sensor.service;

import com.leafree.backend.domain.event.enums.EventContentCode;
import com.leafree.backend.domain.event.service.EventService;
import com.leafree.backend.domain.sensor.entity.Sensor;
import com.leafree.backend.domain.setting.entity.Setting;
import com.leafree.backend.domain.setting.repository.SettingRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorBufferService {
    private final Map<Long, Sensor> sensorBuffer = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final SensorService sensorService;
    private final SettingRepository settingRepository;
    private final EventService eventService;
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void init() {
        // 1초마다 버퍼된 데이터 전송
        scheduler.scheduleAtFixedRate(this::flushBuffer, 0, 1, TimeUnit.SECONDS);
        log.info("SensorBufferService initialized");
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("SensorBufferService destroyed");
    }

    public void bufferSensorData(Long deviceId, String content, float value) {
        if (!Float.isFinite(value) || !isSupportedSensorType(content)) {
            log.warn("Rejected invalid sensor payload: deviceId={}, type={}, value={}", deviceId, content, value);
            return;
        }
        sensorBuffer.compute(deviceId, (key, existingSensor) -> {
            Sensor.SensorBuilder builder = existingSensor != null ? 
                Sensor.builder()
                    .deviceId(existingSensor.getDeviceId())
                    .temperature(existingSensor.getTemperature())
                    .humidity(existingSensor.getHumidity())
                    .light(existingSensor.getLight())
                    .co2Level(existingSensor.getCo2Level())
                    .waterLevel(existingSensor.getWaterLevel())
                    .soilMoisture(existingSensor.getSoilMoisture()) :
                Sensor.builder()
                    .deviceId(deviceId);

            // 센서 타입에 따라 값 설정
            switch (content) {
                case "temperature" -> builder.temperature(value);
                case "humidity" -> builder.humidity((int) value);
                case "light" -> builder.light((int) value);
                case "co2_ppm" -> builder.co2Level((int) value);
                case "water_level" -> builder.waterLevel((int) value);
                case "soil_moisture" -> builder.soilMoisture((int) value);
                default -> { }
            }
            return builder.build();
        });
    }

    private boolean isSupportedSensorType(String content) {
        return "temperature".equals(content) || "humidity".equals(content)
                || "light".equals(content) || "co2_ppm".equals(content)
                || "water_level".equals(content) || "soil_moisture".equals(content);
    }

    private void flushBuffer() {
        if (sensorBuffer.isEmpty()) return;

        // Remove only the exact snapshot after it has been persisted. A newer
        // MQTT sample for the same device must remain buffered for the next run.
        Map<Long, Sensor> snapshot = new HashMap<>(sensorBuffer);
        snapshot.forEach((deviceId, sensor) -> {
            try {
                Optional<Setting> optionalSetting = settingRepository.findById(deviceId);
                if (optionalSetting.isEmpty()) {
                    log.warn("Setting not found for deviceId: {}", deviceId);
                    sensorBuffer.remove(deviceId, sensor);
                    return;
                }

                Setting setting = optionalSetting.get();

                if (sensor.getTemperature() != null){
                    handleWarning(deviceId, "temperature", sensor.getTemperature(),
                            setting.getOptimalTemperature(), 10,
                            EventContentCode.TEMP_HIGH, EventContentCode.TEMP_LOW);
                }

                if (sensor.getHumidity() != null){
                    handleWarning(deviceId, "humidity", sensor.getHumidity(),
                            setting.getOptimalHumidity(), 10,
                            EventContentCode.HUM_HIGH, EventContentCode.HUM_LOW);
                }

                if (sensor.getWaterLevel() != null){
                    handleWarning(deviceId, "water_level", sensor.getWaterLevel(),
                            20, 0,  // 기준: 20, 허용오차 없음
                            null, EventContentCode.WATER_LOW);
                }

                // DB 저장
                Sensor savedSensor = sensorService.saveSensorData(sensor);
                sensorBuffer.remove(deviceId, sensor);

            } catch (Exception e) {
                log.error("Failed to process sensor data for device {}: {}", deviceId, e.getMessage());
            }
        });

    }

    private void handleWarning(Long deviceId, String type, float value,
                               float standard, int tolerance,
                               EventContentCode highCode, EventContentCode lowCode) {
        String key = "sensor:warning:%d:%s".formatted(deviceId, type);
        String prev = redisTemplate.opsForValue().get(key);

        if (value > standard + tolerance) {
            if (!"HIGH".equals(prev)) {
                if (highCode != null) {
                    eventService.logWarningEvent(deviceId, highCode, value);
                    log.info("⚠️ HIGH warning triggered for {}: {}", type, value);
                }
                redisTemplate.opsForValue().set(key, "HIGH");
            }
        } else if (value < standard - tolerance) {
            if (!"LOW".equals(prev)) {
                if (lowCode != null) {
                    eventService.logWarningEvent(deviceId, lowCode, value);
                    log.info("⚠️ LOW warning triggered for {}: {}", type, value);
                }
                redisTemplate.opsForValue().set(key, "LOW");
            }
        } else {
            redisTemplate.delete(key);
        }
    }


}
