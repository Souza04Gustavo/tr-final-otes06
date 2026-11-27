package br.udesc.casaemdia.ui.main;

import androidx.annotation.Nullable;

/**
 * O que as abas (fragments) podem pedir à tela principal, sem depender da classe concreta.
 */
public interface TelaPrincipal {

    void definirCabecalho(String titulo, @Nullable String subtitulo);

    void irParaLembretes();

    void irParaCompras();

    void irParaGastos();
}
