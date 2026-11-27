package br.udesc.casaemdia.common.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extrai o id do usuário autenticado a partir do JWT (claim {@code sub}).
 * O id do usuário nunca vem do corpo da requisição nem da URL (RN01).
 */
public final class UsuarioLogado {

    private UsuarioLogado() {
    }

    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
