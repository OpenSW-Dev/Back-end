package com.opensw.food.api.member.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtProvider {

    private final String secretKey;
    private final long expiration;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.expiration}") long expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
    }

    public String generateToken(String subject, Map<String, Object> claims,
                                Instant expireTime) {
        return Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(expireTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException  e) {
            System.out.println("JWT Token Expired");
            return false;
        } catch (MalformedJwtException  e) {
            System.out.println("Invalid JWT Token");
            return false;
        } catch (UnsupportedJwtException  e) {
            System.out.println("Unsupported JWT Token");
            return false;
        } catch (IllegalArgumentException  e) {
            System.out.println("JWT claims string is empty");
            return false;
        }
    }

    public String getEmailFromClaims(Claims claims) {
        return claims.getSubject();
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromClaims(Claims claims) {
        String role = claims.get("role", String.class);
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}
