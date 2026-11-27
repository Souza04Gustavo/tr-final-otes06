package br.udesc.casaemdia.compra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Todas as consultas recebem o id do usuário, garantindo o isolamento dos dados (RN01).
 */
public interface ItemCompraRepository extends JpaRepository<ItemCompra, Long> {

    Optional<ItemCompra> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** Não comprados primeiro; depois, na ordem em que foram adicionados. */
    List<ItemCompra> findByUsuarioIdOrderByCompradoAscCriadoEmAscIdAsc(Long usuarioId);

    long countByUsuarioIdAndCompradoFalse(Long usuarioId);

    long deleteByUsuarioIdAndCompradoTrue(Long usuarioId);
}
