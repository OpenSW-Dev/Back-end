package com.opensw.food.api.member.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // HTTP 요청 헤더에서 JWT 추출
            String jwt = resolveToken(request);

            // JWT 유효성 검증
            if (StringUtils.hasText(jwt) && jwtProvider.isTokenValid(jwt)) {
                // 토큰에서 인증 정보 생성
                Authentication auth = jwtProvider.getAuthentication(jwt);
                // SecurityContextHolder 에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("Security Context: " + SecurityContextHolder.getContext().getAuthentication());
            }
        } catch (Exception e) {
            System.out.println("JWT Filter Exception: " + e.getMessage());
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
