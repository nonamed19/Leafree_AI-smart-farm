package com.leafree.backend.util;

import com.leafree.backend.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

public final class SecurityUtil {
    private SecurityUtil() {
    }

    public static Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails user) {
            return user.getId();
        }
        throw new AccessDeniedException("인증된 사용자를 찾을 수 없습니다.");
    }
}
