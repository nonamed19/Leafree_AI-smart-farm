package com.leafree.backend.domain.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UpdateSettingResponse {
    float temperature;
    int humidity;
    int light;
    int co2Ppm;
    int soilHumidity;
    int lightDuration;
    int waterInterval;
    LocalDateTime updateAt;
}
