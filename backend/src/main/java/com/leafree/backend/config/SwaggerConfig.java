package com.leafree.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("JWT",createAPIKeyScheme()))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("JWT"))
                .servers(List.of(
                        new Server().url("http://localhost:9000/api").description("로컬서버"),
        new Server().url("https://leafree.kr/api").description("운영서버")
                ))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Backend API")
                .description("Backend API description")
                .version("1.0");
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("Bearer");
    }

}
