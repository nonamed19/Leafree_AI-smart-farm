package com.leafree.backend.domain.device.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DeviceDetailResponseDto {
    private String deviceUuid;
    private Long cropId;
    private String cropType;
    private String cropStatus;
    private LocalDateTime createdAt;
    private LocalDate harvestedAt;
}