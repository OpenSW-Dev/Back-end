package com.opensw.food.api.member.jwt;

import com.opensw.food.common.exception.UnauthorizedException;
import com.opensw.food.common.response.ErrorStatus;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class JwtProvider {

    private final String secretKey;
    private final long expiration;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.expiration}") long expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException  e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.TOKEN_EXPIRED.getMessage());
        } catch (MalformedJwtException  e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.INVALID_TOKEN.getMessage());
        } catch (UnsupportedJwtException  e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException  e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new UnauthorizedException(ErrorStatus.EMPTY_TOKEN.getMessage());
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        User principal = new User(email, "",
                Collections.singletonList(new SimpleGrantedAuthority(role)));

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
