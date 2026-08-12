package com.leafree.backend.domain.user.service;

import com.leafree.backend.domain.user.dto.LogInRequest;
import com.leafree.backend.domain.user.dto.LogInResponse;
import jakarta.validation.Valid;

public interface AuthService {
    LogInResponse getUser(@Valid LogInRequest request);
}
