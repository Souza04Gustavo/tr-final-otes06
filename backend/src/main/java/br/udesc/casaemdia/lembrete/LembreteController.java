package br.udesc.casaemdia.lembrete;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.casaemdia.common.security.UsuarioLogado;
import br.udesc.casaemdia.config.OpenApiConfig;
import br.udesc.casaemdia.lembrete.dto.ConclusaoResponse;
import br.udesc.casaemdia.lembrete.dto.LembreteRequest;
import br.udesc.casaemdia.lembrete.dto.LembreteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lembretes")
@Tag(name = "Lembretes")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_SEGURANCA)
public class LembreteController {

    private final LembreteService lembreteService;

    public LembreteController(LembreteService lembreteService) {
        this.lembreteService = lembreteService;
    }

    @GetMapping
    @Operation(summary = "Lista os lembretes, filtrando por categoria e situação, em ordem de vencimento")
    public List<LembreteResponse> listar(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(defaultValue = "false") boolean concluido) {
        return lembreteService.listar(UsuarioLogado.id(jwt), categoria, concluido);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um lembrete")
    public LembreteResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return lembreteService.buscar(UsuarioLogado.id(jwt), id);
    }

    @PostMapping
    @Operation(summary = "Cria um lembrete pendente")
    public ResponseEntity<LembreteResponse> criar(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LembreteRequest request) {
        LembreteResponse criado = lembreteService.criar(UsuarioLogado.id(jwt), request);
        return ResponseEntity.created(URI.create("/api/lembretes/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita os dados de um lembrete (não altera a situação)")
    public LembreteResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @Valid @RequestBody LembreteRequest request) {
        return lembreteService.atualizar(UsuarioLogado.id(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um lembrete")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        lembreteService.excluir(UsuarioLogado.id(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    @Operation(summary = "Conclui o lembrete e, se for recorrente, cria a próxima ocorrência")
    public ConclusaoResponse concluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return lembreteService.concluir(UsuarioLogado.id(jwt), id);
    }

    @PatchMapping("/{id}/reabrir")
    @Operation(summary = "Desfaz a conclusão: volta a pendente e remove a próxima ocorrência gerada, se ainda pendente")
    public LembreteResponse reabrir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return lembreteService.reabrir(UsuarioLogado.id(jwt), id);
    }
}
