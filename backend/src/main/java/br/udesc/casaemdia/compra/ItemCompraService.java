package br.udesc.casaemdia.compra;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.casaemdia.common.exception.RecursoNaoEncontradoException;
import br.udesc.casaemdia.compra.dto.ItemCompraRequest;
import br.udesc.casaemdia.compra.dto.ItemCompraResponse;
import br.udesc.casaemdia.usuario.UsuarioRepository;

@Service
public class ItemCompraService {

    private final ItemCompraRepository itemCompraRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public ItemCompraService(ItemCompraRepository itemCompraRepository, UsuarioRepository usuarioRepository,
            Clock clock) {
        this.itemCompraRepository = itemCompraRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ItemCompraResponse> listar(Long usuarioId) {
        return itemCompraRepository.findByUsuarioIdOrderByCompradoAscCriadoEmAscIdAsc(usuarioId).stream()
                .map(ItemCompraResponse::from)
                .toList();
    }

    @Transactional
    public ItemCompraResponse criar(Long usuarioId, ItemCompraRequest request) {
        ItemCompra item = new ItemCompra(
                usuarioRepository.getReferenceById(usuarioId),
                request.nome().trim(),
                request.quantidadeOuPadrao(),
                LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS));
        return ItemCompraResponse.from(itemCompraRepository.save(item));
    }

    @Transactional
    public ItemCompraResponse atualizar(Long usuarioId, Long id, ItemCompraRequest request) {
        ItemCompra item = buscarEntidade(usuarioId, id);
        item.atualizar(request.nome().trim(), request.quantidadeOuPadrao());
        return ItemCompraResponse.from(item);
    }

    @Transactional
    public ItemCompraResponse alternarComprado(Long usuarioId, Long id) {
        ItemCompra item = buscarEntidade(usuarioId, id);
        item.alternarComprado();
        return ItemCompraResponse.from(item);
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        itemCompraRepository.delete(buscarEntidade(usuarioId, id));
    }

    /** RN11: remove todos os itens já comprados do usuário. */
    @Transactional
    public void limparComprados(Long usuarioId) {
        itemCompraRepository.deleteByUsuarioIdAndCompradoTrue(usuarioId);
    }

    @Transactional(readOnly = true)
    public long contarPendentes(Long usuarioId) {
        return itemCompraRepository.countByUsuarioIdAndCompradoFalse(usuarioId);
    }

    private ItemCompra buscarEntidade(Long usuarioId, Long id) {
        return itemCompraRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item da lista de compras não encontrado"));
    }
}
