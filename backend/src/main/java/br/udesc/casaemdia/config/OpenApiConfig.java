package br.udesc.casaemdia.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Metadados da documentação OpenAPI/Swagger e o esquema de autenticação Bearer,
 * que habilita o botão "Authorize" no Swagger UI.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Casa em Dia API",
        version = "1.0.0",
        description = "API REST do app Casa em Dia: lembretes da casa (contas, manutenções e outros), "
                + "lista de compras e painel. Projeto final da disciplina OTES06 – UDESC."))
@SecurityScheme(
        name = OpenApiConfig.ESQUEMA_SEGURANCA,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    public static final String ESQUEMA_SEGURANCA = "bearerAuth";
}
