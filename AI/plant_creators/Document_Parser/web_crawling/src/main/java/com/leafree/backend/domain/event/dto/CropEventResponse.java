package com.leafree.backend.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class CropEventResponse {
    String cropType;
    LocalDate startDate;
    LocalDate endDate;
}
