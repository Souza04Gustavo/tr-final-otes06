package br.udesc.casaemdia.util;

import androidx.annotation.Nullable;

/**
 * Mensagem curta para o usuário (Snackbar), opcionalmente com a ação "Desfazer".
 */
public final class Aviso {

    private final String texto;
    @Nullable
    private final Runnable desfazer;

    private Aviso(String texto, @Nullable Runnable desfazer) {
        this.texto = texto;
        this.desfazer = desfazer;
    }

    public static Aviso simples(String texto) {
        return new Aviso(texto, null);
    }

    public static Aviso comDesfazer(String texto, Runnable desfazer) {
        return new Aviso(texto, desfazer);
    }

    public String getTexto() {
        return texto;
    }

    /** Ação executada ao tocar em "Desfazer"; {@code null} quando o aviso não tem essa opção. */
    @Nullable
    public Runnable getDesfazer() {
        return desfazer;
    }
}
