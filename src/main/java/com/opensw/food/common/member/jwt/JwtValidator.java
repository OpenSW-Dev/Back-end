package com.opensw.food.common.member.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {

    private final JwtProvider jwtProvider;

    public JwtValidator(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public boolean validateToken(String token) {
        try {
            jwtProvider.parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getValidatedClaims(String token) {
        if (!validateToken(token)) {
            throw new JwtException("Invalid JWT Token");
        }
        return jwtProvider.parseToken(token);
    }
}
