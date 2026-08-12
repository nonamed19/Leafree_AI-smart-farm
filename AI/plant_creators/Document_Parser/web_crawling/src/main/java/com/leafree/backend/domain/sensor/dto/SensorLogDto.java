package com.leafree.backend.domain.sensor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorLogDto {
    float temperature;
    int humidity;
    int light;
    int co2Ppm;
    int waterLevel;
    int soilMoisture;
}
