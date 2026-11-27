package br.udesc.casaemdia.auth;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.casaemdia.auth.TokenService.TokenGerado;
import br.udesc.casaemdia.auth.dto.AuthResponse;
import br.udesc.casaemdia.auth.dto.CadastroRequest;
import br.udesc.casaemdia.auth.dto.LoginRequest;
import br.udesc.casaemdia.common.exception.CredenciaisInvalidasException;
import br.udesc.casaemdia.common.exception.EmailJaCadastradoException;
import br.udesc.casaemdia.usuario.Usuario;
import br.udesc.casaemdia.usuario.UsuarioRepository;
import br.udesc.casaemdia.usuario.dto.UsuarioResponse;

@Service
public class AuthService {

    private static final String TIPO_TOKEN = "Bearer";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Clock clock;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            TokenService tokenService, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    /** Cria a conta (RN02) e já devolve o token, para o app entrar direto. */
    @Transactional
    public AuthResponse cadastrar(CadastroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = new Usuario(
                request.nome().trim(),
                request.nomeCasa().trim(),
                email,
                passwordEncoder.encode(request.senha()),
                LocalDateTime.now(clock));
        return autenticar(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(request.email()))
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return autenticar(usuario);
    }

    private AuthResponse autenticar(Usuario usuario) {
        TokenGerado token = tokenService.gerar(usuario);
        return new AuthResponse(token.token(), TIPO_TOKEN, token.expiraEm(), UsuarioResponse.from(usuario));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
