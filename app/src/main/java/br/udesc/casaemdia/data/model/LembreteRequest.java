package br.udesc.casaemdia.data.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Corpo enviado para criar ou editar um lembrete. */
public class LembreteRequest {

    private final String titulo;
    private final Categoria categoria;
    private final LocalDate dataVencimento;
    private final BigDecimal valor;
    private final Recorrencia recorrencia;
    private final Integer intervaloDias;
    private final String observacao;

    public LembreteRequest(String titulo, Categoria categoria, LocalDate dataVencimento, BigDecimal valor,
            Recorrencia recorrencia, Integer intervaloDias, String observacao) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.dataVencimento = dataVencimento;
        this.valor = valor;
        this.recorrencia = recorrencia;
        this.intervaloDias = intervaloDias;
        this.observacao = observacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Recorrencia getRecorrencia() {
        return recorrencia;
    }

    public Integer getIntervaloDias() {
        return intervaloDias;
    }

    public String getObservacao() {
        return observacao;
    }
}
