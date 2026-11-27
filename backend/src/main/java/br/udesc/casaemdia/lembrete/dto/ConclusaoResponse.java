package br.udesc.casaemdia.lembrete.dto;

/**
 * Resultado de concluir um lembrete.
 *
 * @param concluido o lembrete que acabou de ser concluído
 * @param proximo   a próxima ocorrência criada, ou {@code null} se o lembrete for pontual
 */
public record ConclusaoResponse(LembreteResponse concluido, LembreteResponse proximo) {
}
