package com.leafree.backend.domain.crop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CropEnvironmentWrapperDto {

    @JsonProperty("plant_name")
    private String cropType;

    @JsonProperty("environment")
    private CropEnvironmentDto environment;
}
