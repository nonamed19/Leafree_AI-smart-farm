package com.leafree.backend.domain.setting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "optimal_temperature")
    private Float optimalTemperature;

    @Column(name = "optimal_humidity")
    private Integer optimalHumidity;

    @Column(name = "optimal_light")
    private Integer optimalLight;

    @Column(name = "optimal_co2_ppm")
    private Integer optimalCo2Ppm;

    @Column(name = "light_duration")
    private Integer lightDuration;

    @Column(name = "watering_interval")
    private Integer wateringInterval;

    @Column(name = "optimal_soil_humidity")
    private Integer optimalSoilHumidity;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void setOptimalTemperature(Float optimalTemperature) {
        this.optimalTemperature = optimalTemperature;
    }

    public void setOptimalHumidity(Integer optimalHumidity) {
        this.optimalHumidity = optimalHumidity;
    }

    public void setOptimalLight(Integer optimalLight) {
        this.optimalLight = optimalLight;
    }

    public void setOptimalCo2Ppm(Integer optimalCo2Ppm) {
        this.optimalCo2Ppm = optimalCo2Ppm;
    }

    public void setLightDuration(Integer lightDuration) {
        this.lightDuration = lightDuration;
    }

    public void setWateringInterval(Integer wateringInterval) {
        this.wateringInterval = wateringInterval;
    }

    public void setOptimalSoilHumidity(Integer optimalSoilHumidity) {
        this.optimalSoilHumidity = optimalSoilHumidity;
    }

}
