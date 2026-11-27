package br.udesc.casaemdia.auth;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.casaemdia.auth.dto.AuthResponse;
import br.udesc.casaemdia.auth.dto.CadastroRequest;
import br.udesc.casaemdia.auth.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cria uma conta e devolve o token de acesso")
    public ResponseEntity<AuthResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        AuthResponse resposta = authService.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/usuarios/me")).body(resposta);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica com e-mail e senha e devolve o token de acesso")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
