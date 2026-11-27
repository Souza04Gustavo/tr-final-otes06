package br.udesc.casaemdia.data.model;

/** Resultado de concluir um lembrete; {@code proximo} é nulo quando o lembrete é pontual. */
public class ConclusaoDto {

    private LembreteDto concluido;
    private LembreteDto proximo;

    public LembreteDto getConcluido() {
        return concluido;
    }

    public LembreteDto getProximo() {
        return proximo;
    }
}
