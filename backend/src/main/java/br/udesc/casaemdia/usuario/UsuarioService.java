package br.udesc.casaemdia.usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.casaemdia.common.exception.RecursoNaoEncontradoException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
