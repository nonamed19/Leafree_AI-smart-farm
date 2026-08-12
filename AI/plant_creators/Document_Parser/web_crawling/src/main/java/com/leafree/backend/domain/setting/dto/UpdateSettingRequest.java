package com.leafree.backend.domain.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdateSettingRequest {
    Float temperature;
    Integer humidity;
    Integer light;
    Integer co2Ppm;
    Integer soilHumidity;
    Integer lightDuration;
    Integer wateringInterval;
}
