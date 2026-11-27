package br.udesc.casaemdia.compra;

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
import org.springframework.web.bind.annotation.RestController;

import br.udesc.casaemdia.common.security.UsuarioLogado;
import br.udesc.casaemdia.compra.dto.ItemCompraRequest;
import br.udesc.casaemdia.compra.dto.ItemCompraResponse;
import br.udesc.casaemdia.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compras")
@Tag(name = "Lista de compras")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_SEGURANCA)
public class ItemCompraController {

    private final ItemCompraService itemCompraService;

    public ItemCompraController(ItemCompraService itemCompraService) {
        this.itemCompraService = itemCompraService;
    }

    @GetMapping
    @Operation(summary = "Lista os itens: não comprados primeiro, depois na ordem de inclusão")
    public List<ItemCompraResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return itemCompraService.listar(UsuarioLogado.id(jwt));
    }

    @PostMapping
    @Operation(summary = "Adiciona um item à lista")
    public ResponseEntity<ItemCompraResponse> criar(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ItemCompraRequest request) {
        ItemCompraResponse criado = itemCompraService.criar(UsuarioLogado.id(jwt), request);
        return ResponseEntity.created(URI.create("/api/compras/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita nome e quantidade de um item")
    public ItemCompraResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @Valid @RequestBody ItemCompraRequest request) {
        return itemCompraService.atualizar(UsuarioLogado.id(jwt), id, request);
    }

    @PatchMapping("/{id}/alternar")
    @Operation(summary = "Marca ou desmarca o item como comprado")
    public ItemCompraResponse alternar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return itemCompraService.alternarComprado(UsuarioLogado.id(jwt), id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um item")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        itemCompraService.excluir(UsuarioLogado.id(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comprados")
    @Operation(summary = "Exclui todos os itens já comprados")
    public ResponseEntity<Void> limparComprados(@AuthenticationPrincipal Jwt jwt) {
        itemCompraService.limparComprados(UsuarioLogado.id(jwt));
        return ResponseEntity.noContent().build();
    }
}
