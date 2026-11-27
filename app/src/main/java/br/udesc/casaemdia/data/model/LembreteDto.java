package br.udesc.casaemdia.data.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Lembrete como devolvido pela API. {@code atrasado} é calculado pelo back-end. */
public class LembreteDto {

    private Long id;
    private String titulo;
    private Categoria categoria;
    private LocalDate dataVencimento;
    private BigDecimal valor;
    private Recorrencia recorrencia;
    private Integer intervaloDias;
    private String observacao;
    private boolean concluido;
    private LocalDate dataConclusao;
    private boolean atrasado;

    public Long getId() {
        return id;
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

    public boolean isConcluido() {
        return concluido;
    }

    public LocalDate getDataConclusao() {
        return dataConclusao;
    }

    public boolean isAtrasado() {
        return atrasado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LembreteDto)) {
            return false;
        }
        LembreteDto outro = (LembreteDto) o;
        return concluido == outro.concluido
                && atrasado == outro.atrasado
                && Objects.equals(id, outro.id)
                && Objects.equals(titulo, outro.titulo)
                && categoria == outro.categoria
                && Objects.equals(dataVencimento, outro.dataVencimento)
                && Objects.equals(valor, outro.valor)
                && recorrencia == outro.recorrencia
                && Objects.equals(intervaloDias, outro.intervaloDias)
                && Objects.equals(observacao, outro.observacao)
                && Objects.equals(dataConclusao, outro.dataConclusao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, categoria, dataVencimento, valor, recorrencia, intervaloDias,
                observacao, concluido, dataConclusao, atrasado);
    }
}
