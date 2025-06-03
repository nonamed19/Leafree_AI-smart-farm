package com.leafree.backend.domain.crop.repository;

import com.leafree.backend.domain.crop.entity.Crop;
import com.leafree.backend.domain.crop.enums.CropStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {

    Crop findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(Long deviceId, CropStatus status);

    Optional<Crop> findTopByDeviceIdAndStatusOrderByCreatedAtDesc(Long deviceId, CropStatus status);

}
