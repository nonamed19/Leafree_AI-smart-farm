package com.leafree.backend.domain.sensor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "temperature")
    private Float temperature;

    @Column(name = "humidity")
    private Integer humidity;

    @Column(name = "light")
    private Integer light;

    @Column(name = "co2_level")
    private Integer co2Level;

    @Column(name = "water_level")
    private Integer waterLevel;

    @Column(name = "soil_moisture")
    private Integer soilMoisture;

    @CreatedDate
    @Column(name = "logged_at", nullable = false, updatable = false)
    private LocalDateTime loggedAt;

}
