package br.udesc.casaemdia.data.model;

import java.time.LocalDateTime;

/** Resposta de login e cadastro: token JWT + dados do usuário. */
public class AuthResponse {

    private String token;
    private String tipo;
    private LocalDateTime expiraEm;
    private UsuarioDto usuario;

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public UsuarioDto getUsuario() {
        return usuario;
    }
}
