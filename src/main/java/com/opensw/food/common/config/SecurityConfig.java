package com.opensw.food.common.config;

import com.opensw.food.api.member.jwt.JwtFilter;
import com.opensw.food.api.member.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration configuration = new CorsConfiguration();
                            configuration.addAllowedOrigin("http://localhost:8080"); // 로컬 테스트 CORS
                            configuration.addAllowedMethod("https://food-social.kro.kr"); // 배포 후 CORS
                            configuration.addAllowedMethod("127.0.0.1:5500"); // 프론트 내부 테스트 CORS
                            return configuration;
                        }))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())) // H2 콘솔 허용
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/**").permitAll()    // 인증 관련 엔드포인트
                        .requestMatchers("/api/v1/public/**").permitAll()  // 공개 API 엔드포인트
                        .requestMatchers("/h2-console/**").permitAll()  // H2 콘솔 접근 허용
                        .requestMatchers("/api/v1/article/detail", "/api/v1/article/total").permitAll() // 게시글 전체, 상세 조회 접근 허용
                        .requestMatchers( "/api-doc","/v3/api-docs/**", "/swagger-resources/**","/swagger-ui/**").permitAll() // 스웨거 접근 허용
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
