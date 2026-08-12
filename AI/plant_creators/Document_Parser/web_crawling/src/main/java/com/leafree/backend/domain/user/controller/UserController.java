package com.leafree.backend.domain.user.controller;

import com.leafree.backend.domain.user.dto.SignUpRequest;
import com.leafree.backend.domain.user.dto.SignUpResponse;
import com.leafree.backend.domain.user.dto.UserCacheDto;
import com.leafree.backend.domain.user.dto.UserInfoResponse;
import com.leafree.backend.domain.user.service.UserService;
import com.leafree.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name="User API", description = "유저 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호로 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse response = userService.createUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인 유저 확인", description = "현재 로그인 유저 정보를 확인합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        Long userId = SecurityUtil.getCurrentUserId(authentication);

        UserCacheDto nowUser = userService.getUserCache(String.valueOf(userId));
        UserInfoResponse response = UserInfoResponse.builder()
                .userId(userId)
                .email(nowUser.email())
                .nickname(nowUser.nickname())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



}
