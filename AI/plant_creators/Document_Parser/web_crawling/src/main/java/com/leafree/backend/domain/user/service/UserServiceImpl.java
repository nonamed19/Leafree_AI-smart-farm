package com.leafree.backend.domain.user.service;

import com.leafree.backend.domain.user.dto.FcmTokenDto;
import com.leafree.backend.domain.user.entity.FcmToken;
import com.leafree.backend.domain.user.repository.FcmTokenRepository;
import com.leafree.backend.exception.ExceptionCode;
import com.leafree.backend.exception.type.BusinessException;
import com.leafree.backend.security.JwtProvider;
import com.leafree.backend.domain.user.dto.SignUpRequest;
import com.leafree.backend.domain.user.dto.SignUpResponse;
import com.leafree.backend.domain.user.dto.UserCacheDto;
import com.leafree.backend.domain.user.entity.User;
import com.leafree.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final FcmTokenRepository fcmTokenRepository;

    @Override
    public SignUpResponse createUser(SignUpRequest signUpRequest) {

        if(userRepository.existsByEmail(signUpRequest.getEmail())){
            throw new BusinessException(ExceptionCode.EMAIL_DUPLICATED);
        }

        User newUser = User.builder()
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .nickname(signUpRequest.getNickname())
                .build();


        User savedUser = userRepository.save(newUser);

        String accessToken = jwtProvider.generateAccessToken(savedUser.getId());

        return SignUpResponse.builder()
                .userId(String.valueOf(savedUser.getId()))
                .accessToken(accessToken)
                .build();
    }

    @Override
    @Cacheable(value = "userCache", key = "#userId")
    public UserCacheDto getUserCache(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        return new UserCacheDto(user.getId(), user.getEmail(), user.getNickname());
    }

    @Override
    public Void saveFcmToken(FcmTokenDto fcmTokenDto, Long deviceId, Long userId) {

        FcmToken fcmToken = FcmToken.builder()
                .userId(userId)
                .fcmToken(fcmTokenDto.getFcmToken())
                .build();

        fcmTokenRepository.save(fcmToken);

        return null;
    }
}
