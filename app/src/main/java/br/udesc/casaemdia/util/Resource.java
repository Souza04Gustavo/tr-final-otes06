package br.udesc.casaemdia.util;

import androidx.annotation.Nullable;

/**
 * Estado de uma operação assíncrona exposto pelos ViewModels para as telas:
 * carregando, sucesso (com os dados) ou erro (com a mensagem).
 */
public final class Resource<T> {

    public enum Status {
        CARREGANDO,
        SUCESSO,
        ERRO
    }

    private final Status status;
    @Nullable
    private final T dados;
    @Nullable
    private final String mensagem;

    private Resource(Status status, @Nullable T dados, @Nullable String mensagem) {
        this.status = status;
        this.dados = dados;
        this.mensagem = mensagem;
    }

    public static <T> Resource<T> carregando() {
        return new Resource<>(Status.CARREGANDO, null, null);
    }

    public static <T> Resource<T> sucesso(@Nullable T dados) {
        return new Resource<>(Status.SUCESSO, dados, null);
    }

    public static <T> Resource<T> erro(String mensagem) {
        return new Resource<>(Status.ERRO, null, mensagem);
    }

    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getDados() {
        return dados;
    }

    @Nullable
    public String getMensagem() {
        return mensagem;
    }

    public boolean isCarregando() {
        return status == Status.CARREGANDO;
    }
}
