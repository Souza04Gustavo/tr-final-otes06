package br.udesc.casaemdia.util;

import androidx.annotation.Nullable;

/**
 * Envolve um valor exposto por LiveData que deve ser consumido uma única vez
 * (ex.: mensagem de Snackbar), evitando que reapareça ao girar a tela.
 */
public class Evento<T> {

    private final T conteudo;
    private boolean tratado;

    public Evento(T conteudo) {
        this.conteudo = conteudo;
    }

    /** Devolve o conteúdo na primeira chamada e {@code null} nas seguintes. */
    @Nullable
    public T consumir() {
        if (tratado) {
            return null;
        }
        tratado = true;
        return conteudo;
    }
}
