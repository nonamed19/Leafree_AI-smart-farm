package com.leafree.backend.domain.user.service;

import com.leafree.backend.exception.ExceptionCode;
import com.leafree.backend.exception.type.BusinessException;
import com.leafree.backend.security.JwtProvider;
import com.leafree.backend.domain.user.dto.LogInRequest;
import com.leafree.backend.domain.user.dto.LogInResponse;
import com.leafree.backend.domain.user.entity.User;
import com.leafree.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;

    @Override
    public LogInResponse getUser(LogInRequest request) {
        User user = userRepository.findByEmail(request.getEmail()) .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        if(!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(ExceptionCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId());

        return LogInResponse.builder()
                .userId(String.valueOf(user.getId()))
                .accessToken(accessToken)
                .build();
    }
}
