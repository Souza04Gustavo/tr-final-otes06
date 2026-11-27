package br.udesc.casaemdia.ui;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.util.Aviso;
import br.udesc.casaemdia.util.Evento;

/**
 * Exibe os {@link Aviso}s dos ViewModels em uma Snackbar, com o botão "Desfazer" quando houver.
 */
public final class Avisos {

    private Avisos() {
    }

    /** Mostra o aviso do evento, se ele ainda não tiver sido exibido. */
    public static void mostrar(View raiz, Evento<Aviso> evento) {
        Aviso aviso = evento.consumir();
        if (aviso == null) {
            return;
        }
        Snackbar snackbar = criar(raiz, aviso.getTexto());
        Runnable desfazer = aviso.getDesfazer();
        if (desfazer != null) {
            snackbar.setAction(R.string.acao_desfazer, v -> desfazer.run());
        }
        snackbar.show();
    }

    /**
     * Snackbar longa que, nas abas da tela principal, aparece acima da navegação inferior
     * em vez de cobri-la.
     */
    public static Snackbar criar(View raiz, CharSequence texto) {
        Snackbar snackbar = Snackbar.make(raiz, texto, Snackbar.LENGTH_LONG);
        View navegacao = raiz.getRootView().findViewById(R.id.navegacao_inferior);
        if (navegacao != null) {
            snackbar.setAnchorView(navegacao);
        }
        return snackbar;
    }
}
