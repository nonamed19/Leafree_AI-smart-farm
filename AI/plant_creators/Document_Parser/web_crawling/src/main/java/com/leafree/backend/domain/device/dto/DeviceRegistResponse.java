package com.leafree.backend.domain.device.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class DeviceRegistResponse {
    private Long deviceId;
    private String deviceUuid;
}
