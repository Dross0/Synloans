package com.synloans.loans.configuration.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfiguration {

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                        .info(new Info()
                                .title("Сервис синдицированного кредитования")
                                .description("Обслуживание синдицированных кредитов с распределенным реестром")
                                .version("v0.0.1")
                                .contact(new Contact()
                                        .email("a.gaidamaka1@g.nsu.ru")
                                        .name("Andrew Gaidamaka")
                                )
                        ).components(new Components()
                                .addSecuritySchemes(
                                        "Authorization",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                        );
        }
}
