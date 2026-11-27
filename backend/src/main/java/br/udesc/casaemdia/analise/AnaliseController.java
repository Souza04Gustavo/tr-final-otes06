package br.udesc.casaemdia.analise;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.casaemdia.analise.dto.AnaliseGastosResponse;
import br.udesc.casaemdia.common.security.UsuarioLogado;
import br.udesc.casaemdia.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/analises")
@Tag(name = "Análises")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_SEGURANCA)
public class AnaliseController {

    private final AnaliseService analiseService;

    public AnaliseController(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    @GetMapping("/gastos")
    @Operation(summary = "Gastos do mês: totais, variação, distribuição por categoria, maiores gastos e histórico")
    public AnaliseGastosResponse gastos(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Mês analisado (yyyy-MM). Padrão: mês atual", example = "2026-11")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @Parameter(description = "Meses no histórico, incluindo o analisado (2 a 12)", example = "6")
            @RequestParam(defaultValue = "" + AnaliseService.MESES_PADRAO) int meses) {
        return analiseService.analisar(UsuarioLogado.id(jwt), mes, meses);
    }
}
