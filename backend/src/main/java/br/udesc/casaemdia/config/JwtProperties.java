package br.udesc.casaemdia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades do JWT definidas em {@code application.properties} com o prefixo {@code app.jwt}.
 *
 * @param secret         segredo usado na assinatura HS256 (mínimo de 32 caracteres)
 * @param expiracaoHoras validade do token em horas
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expiracaoHoras) {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    public JwtProperties {
        if (secret == null || secret.length() < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "app.jwt.secret deve ter pelo menos " + TAMANHO_MINIMO_SEGREDO + " caracteres");
        }
        if (expiracaoHoras <= 0) {
            throw new IllegalStateException("app.jwt.expiracao-horas deve ser maior que zero");
        }
    }
}
