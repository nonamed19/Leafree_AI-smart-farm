package com.leafree.backend.domain.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class SettingResponse {
    private Float temperature;
    private Integer humidity;
    private Integer light;
    private Integer co2Ppm;
    private Integer soilHumidity;
    private Integer lightDuration;
    private Integer wateringInterval;
    private LocalDateTime updateAt;
}
