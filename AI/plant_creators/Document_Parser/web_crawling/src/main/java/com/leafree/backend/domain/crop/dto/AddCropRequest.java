package com.leafree.backend.domain.crop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AddCropRequest {
    Long deviceId;
    String cropType;
}
