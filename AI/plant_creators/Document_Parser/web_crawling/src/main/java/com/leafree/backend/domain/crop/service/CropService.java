package com.leafree.backend.domain.crop.service;

import com.leafree.backend.domain.crop.dto.AddCropRequest;
import jakarta.validation.Valid;

public interface CropService {
    void addCrop(@Valid AddCropRequest request);
    void deleteCrop(Long deviceId, Long cropId);

    byte[] getLatestCropImage(Long deviceId);
}
