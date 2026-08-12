package com.leafree.backend.domain.crop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropEnvironmentDto {

    @JsonProperty("AirTemperature")
    private String airTemperature;

    @JsonProperty("AirHumidity")
    private String airHumidity;

    @JsonProperty("Quantum")
    private String quantum;

    @JsonProperty("Quantum_lux")
    private String quantumLux;

    @JsonProperty("HighSoilHumi")
    private String highSoilHumi;

    @JsonProperty("HighEC")
    private String highEc;

    @JsonProperty("HighPH")
    private String highPh;

    @JsonProperty("LowSoilHumi")
    private String lowSoilHumi;

    @JsonProperty("LowSoilEC")
    private String lowEc;

    @JsonProperty("LowSoilPH")
    private String lowPh;

    @JsonProperty("AmtIrrigation")
    private String amtIrrigation;

    @JsonProperty("IrrigationInterval")
    private String irrigationInterval;

    @JsonProperty("Photoperiod")
    private String photoperiod;

    @JsonProperty("CO2Level")
    private String co2Level;

    @JsonProperty("HarvestTime")
    private String harvestTime;

}
