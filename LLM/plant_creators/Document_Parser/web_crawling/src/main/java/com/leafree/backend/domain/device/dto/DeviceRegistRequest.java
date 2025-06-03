package com.leafree.backend.domain.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class DeviceRegistRequest {
    @NotBlank(message = "디바이스 UUID는 필수입니다.")
    @Size(max = 36, message = "UUID는 36자 이하입니다.")
    private String deviceUuid;
}
