package com.opensw.food.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // Access Token Bearer 인증 스키마 설정
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Refresh Token Bearer 인증 스키마 설정
        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization-Refresh");

        // SecurityRequirement 설정 - 각 토큰별 인증 요구사항 추가
        SecurityRequirement accessTokenRequirement = new SecurityRequirement().addList("Authorization");
        SecurityRequirement refreshTokenRequirement = new SecurityRequirement().addList("Authorization-Refresh");

        Server server = new Server();
        server.setUrl("https://food-social.kro.kr");

        return new OpenAPI()
                .info(new Info()
                        .title("음식 공유 커뮤니티")
                        .description("음식 공유 커뮤니티 REST API Document")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("Authorization", accessTokenScheme)
                        .addSecuritySchemes("Authorization-Refresh", refreshTokenScheme))
                .addServersItem(server)
                .addSecurityItem(accessTokenRequirement)
                .addSecurityItem(refreshTokenRequirement);
    }
}
