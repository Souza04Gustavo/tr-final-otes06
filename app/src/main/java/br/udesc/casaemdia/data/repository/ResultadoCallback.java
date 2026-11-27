package br.udesc.casaemdia.data.repository;

import androidx.annotation.Nullable;

/**
 * Resultado de uma chamada assíncrona à API. Os métodos são chamados na thread principal.
 */
public interface ResultadoCallback<T> {

    /** @param dados corpo da resposta ({@code null} em respostas 204) */
    void sucesso(@Nullable T dados);

    void erro(String mensagem);
}
