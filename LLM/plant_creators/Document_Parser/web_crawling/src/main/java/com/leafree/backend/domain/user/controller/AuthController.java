package com.leafree.backend.domain.user.controller;

import com.leafree.backend.domain.user.dto.LogInRequest;
import com.leafree.backend.domain.user.dto.LogInResponse;
import com.leafree.backend.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "인증, 인가 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LogInResponse> login(@Valid @RequestBody LogInRequest request) {
        LogInResponse response = authService.getUser(request);
        return ResponseEntity.ok(response);
    }


}
