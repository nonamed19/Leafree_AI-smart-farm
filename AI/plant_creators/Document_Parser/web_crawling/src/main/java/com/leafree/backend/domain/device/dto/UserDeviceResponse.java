package com.leafree.backend.domain.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserDeviceResponse {
    private Long deviceId;
    private LocalDateTime createdAt;
}
