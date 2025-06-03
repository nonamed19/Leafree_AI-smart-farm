package com.leafree.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min=8, max = 20, message = "비밀번호는 8자 이상 20자 미만이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min=2, max = 20, message = "닉네임은 2자 이상 20자 미만이어야 합니다.")
    private String nickname;
}
