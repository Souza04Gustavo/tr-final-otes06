package br.udesc.casaemdia.painel;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.casaemdia.common.security.UsuarioLogado;
import br.udesc.casaemdia.config.OpenApiConfig;
import br.udesc.casaemdia.painel.dto.PainelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/painel")
@Tag(name = "Painel")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_SEGURANCA)
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping
    @Operation(summary = "Atrasados, vencendo hoje, próximos 7 dias, itens a comprar e resumo das contas do mês")
    public PainelResponse painel(@AuthenticationPrincipal Jwt jwt) {
        return painelService.montar(UsuarioLogado.id(jwt));
    }
}
