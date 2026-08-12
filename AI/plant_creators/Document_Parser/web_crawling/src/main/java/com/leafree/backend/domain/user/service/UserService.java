package com.leafree.backend.domain.user.service;

import com.leafree.backend.domain.user.dto.FcmTokenDto;
import com.leafree.backend.domain.user.dto.SignUpRequest;
import com.leafree.backend.domain.user.dto.SignUpResponse;
import com.leafree.backend.domain.user.dto.UserCacheDto;
import jakarta.validation.Valid;

public interface UserService {
    SignUpResponse createUser(SignUpRequest signUpRequest);
    UserCacheDto getUserCache(String userId);
    Void saveFcmToken(@Valid FcmTokenDto fcmTokenDto, Long deviceId, Long userId);
}
