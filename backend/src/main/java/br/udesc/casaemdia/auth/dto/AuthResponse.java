package br.udesc.casaemdia.auth.dto;

import java.time.LocalDateTime;

import br.udesc.casaemdia.usuario.dto.UsuarioResponse;

public record AuthResponse(String token, String tipo, LocalDateTime expiraEm, UsuarioResponse usuario) {
}
