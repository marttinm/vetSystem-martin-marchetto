package com.vetSystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI vetSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Clinica Veterinaria - Patitas Felices")
                        .description("Documentacion de la API REST de VetSystem: duenios, mascotas, veterinarios y turnos")
                        .version("1.0")
                        .contact(new Contact().name("Martin Marchetto"))
                        .license(new License().name("Uso Academico")));
    }
}
