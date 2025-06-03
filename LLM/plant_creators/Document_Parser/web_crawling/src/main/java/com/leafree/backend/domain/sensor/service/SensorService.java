package com.leafree.backend.domain.sensor.service;

import com.leafree.backend.domain.sensor.entity.Sensor;

public interface SensorService {
    Sensor saveSensorData(String deviceId, String content, float value);
    Sensor saveSensorData(Sensor sensor);
}
