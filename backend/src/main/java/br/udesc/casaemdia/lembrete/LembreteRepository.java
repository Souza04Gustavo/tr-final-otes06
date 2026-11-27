package br.udesc.casaemdia.lembrete;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Todas as consultas recebem o id do usuário, garantindo o isolamento dos dados (RN01).
 */
public interface LembreteRepository extends JpaRepository<Lembrete, Long> {

    Optional<Lembrete> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Lembrete> findByUsuarioIdAndConcluidoOrderByDataVencimentoAscIdAsc(Long usuarioId, boolean concluido);

    List<Lembrete> findByUsuarioIdAndConcluidoAndCategoriaOrderByDataVencimentoAscIdAsc(
            Long usuarioId, boolean concluido, Categoria categoria);

    /** Pendentes com vencimento até a data limite (usado no painel). */
    List<Lembrete> findByUsuarioIdAndConcluidoFalseAndDataVencimentoLessThanEqualOrderByDataVencimentoAscIdAsc(
            Long usuarioId, LocalDate limite);

    /** Ocorrências pendentes geradas ao concluir o lembrete de origem (usado para desfazer a conclusão). */
    List<Lembrete> findByUsuarioIdAndOrigemIdAndConcluidoFalse(Long usuarioId, Long origemId);

    /** Lembretes com valor e vencimento no intervalo (usado na análise de gastos). */
    List<Lembrete> findByUsuarioIdAndValorIsNotNullAndDataVencimentoBetween(
            Long usuarioId, LocalDate inicio, LocalDate fim);

    /** Lembretes de uma categoria com vencimento no intervalo (usado no resumo do mês). */
    List<Lembrete> findByUsuarioIdAndCategoriaAndDataVencimentoBetween(
            Long usuarioId, Categoria categoria, LocalDate inicio, LocalDate fim);
}
