package com.synloans.loans.configuration.api.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.money.MonetaryAmount;


@Configuration
public class OpenApiConfiguration {

        @Bean
        public OpenAPI openAPI() {
                SpringDocUtils.getConfig().replaceWithSchema(
                        MonetaryAmount.class,
                        new ObjectSchema()
                                .addProperty("amount", new NumberSchema().example(99.93))
                                .addProperty("currency", new StringSchema().example("RUR"))
                );
                return new OpenAPI()
                        .info(new Info()
                                .title("Сервис расчета графика платежей по кредиту")
                                .description("Расчет графика платежей по кредиту с аннуитетом и дифференцированными платежеми")
                                .version("v0.0.1")
                                .contact(new Contact()
                                        .email("a.gaidamaka1@g.nsu.ru")
                                        .name("Andrew Gaidamaka")
                                )
                        );
        }
}
