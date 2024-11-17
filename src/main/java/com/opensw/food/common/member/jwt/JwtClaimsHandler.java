package com.opensw.food.common.member.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class JwtClaimsHandler {

    public String getEmailFromClaims(Claims claims) {
        return claims.getSubject();
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromClaims(Claims claims) {
        String role = claims.get("role", String.class);
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}
