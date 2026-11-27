package br.udesc.casaemdia.usuario;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.casaemdia.common.security.UsuarioLogado;
import br.udesc.casaemdia.config.OpenApiConfig;
import br.udesc.casaemdia.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_SEGURANCA)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResponse.from(usuarioService.buscarPorId(UsuarioLogado.id(jwt)));
    }
}
