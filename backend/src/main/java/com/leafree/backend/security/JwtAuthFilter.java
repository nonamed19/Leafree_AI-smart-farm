package com.leafree.backend.security;

import com.leafree.backend.domain.user.dto.UserCacheDto;
import com.leafree.backend.domain.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String token = request.getHeader("Authorization");

        String userId = null;

        if(token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            userId = jwtProvider.getUserIdFromToken(jwtToken);
        }

        if(userId != null && !userId.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(getUserAuth(userId));
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getUserAuth(String userId) {
        UserCacheDto cacheDto = userService.getUserCache(userId);
        CustomUserDetails userDetails = new CustomUserDetails(cacheDto.id(), cacheDto.email(), cacheDto.nickname());
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
