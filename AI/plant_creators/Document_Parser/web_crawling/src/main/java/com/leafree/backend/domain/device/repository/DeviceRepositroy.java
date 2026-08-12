package com.leafree.backend.domain.device.repository;

import com.leafree.backend.domain.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepositroy extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceUuid(String deviceUuid);
}
