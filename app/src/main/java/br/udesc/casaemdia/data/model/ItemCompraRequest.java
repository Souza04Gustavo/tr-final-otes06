package br.udesc.casaemdia.data.model;

public class ItemCompraRequest {

    private final String nome;
    private final Integer quantidade;

    public ItemCompraRequest(String nome, Integer quantidade) {
        this.nome = nome;
        this.quantidade = quantidade;
    }

    public String getNome() {
        return nome;
    }

    public Integer getQuantidade() {
        return quantidade;
    }
}
