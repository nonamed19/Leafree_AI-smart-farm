package com.leafree.backend.domain.sensor.repository;

import com.leafree.backend.domain.sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {


}
