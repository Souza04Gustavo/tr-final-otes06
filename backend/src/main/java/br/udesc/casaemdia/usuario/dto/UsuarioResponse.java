package br.udesc.casaemdia.usuario.dto;

import br.udesc.casaemdia.usuario.Usuario;

public record UsuarioResponse(Long id, String nome, String nomeCasa, String email) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getNomeCasa(), usuario.getEmail());
    }
}
