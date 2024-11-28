package com.opensw.food.api.member.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JwtProvider {

    private final String secretKey;
    private final long expiration;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.expiration}") long expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException  e) {
            System.out.println("JWT Token Expired");
        } catch (MalformedJwtException  e) {
            System.out.println("Invalid JWT Token");
        } catch (UnsupportedJwtException  e) {
            System.out.println("Unsupported JWT Token");
        } catch (IllegalArgumentException  e) {
            System.out.println("JWT claims string is empty");
        }

        return false;
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
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
