package com.leafree.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FcmTokenDto {
    @NotBlank(message = "토큰은 필수입니다.")
    private String fcmToken;
}
