package br.udesc.casaemdia.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.udesc.casaemdia.config.JwtProperties;
import br.udesc.casaemdia.usuario.Usuario;

/**
 * Gera o JWT de acesso. O {@code subject} é o id do usuário.
 */
@Service
public class TokenService {

    private static final String EMISSOR = "casa-em-dia";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties propriedades;
    private final Clock clock;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties propriedades, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
        this.clock = clock;
    }

    public TokenGerado gerar(Usuario usuario) {
        Instant agora = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiracao = agora.plus(Duration.ofHours(propriedades.expiracaoHoras()));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .issuedAt(agora)
                .expiresAt(expiracao)
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenGerado(token, LocalDateTime.ofInstant(expiracao, clock.getZone()));
    }

    public record TokenGerado(String token, LocalDateTime expiraEm) {
    }
}
