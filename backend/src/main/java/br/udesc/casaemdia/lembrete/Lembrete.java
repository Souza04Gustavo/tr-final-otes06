package br.udesc.casaemdia.lembrete;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.udesc.casaemdia.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Lembrete da casa: uma conta, uma manutenção ou outro compromisso com data.
 */
@Entity
@Table(name = "lembretes", indexes = {
        @Index(name = "idx_lembretes_usuario_situacao_data", columnList = "usuario_id, concluido, data_vencimento")
})
public class Lembrete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Recorrencia recorrencia = Recorrencia.NENHUMA;

    @Column(name = "intervalo_dias")
    private Integer intervaloDias;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private boolean concluido;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    /** Lembrete que, ao ser concluído, gerou esta ocorrência (usado para desfazer a conclusão). */
    @Column(name = "origem_id")
    private Long origemId;

    /** Construtor exigido pela JPA. */
    protected Lembrete() {
    }

    public Lembrete(Usuario usuario, String titulo, Categoria categoria, LocalDate dataVencimento,
            BigDecimal valor, Recorrencia recorrencia, Integer intervaloDias, String observacao,
            LocalDateTime criadoEm) {
        this.usuario = usuario;
        this.criadoEm = criadoEm;
        atualizar(titulo, categoria, dataVencimento, valor, recorrencia, intervaloDias, observacao);
    }

    /** Substitui os dados editáveis. Não altera a situação (concluído ou não). */
    public final void atualizar(String titulo, Categoria categoria, LocalDate dataVencimento, BigDecimal valor,
            Recorrencia recorrencia, Integer intervaloDias, String observacao) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.dataVencimento = dataVencimento;
        this.valor = valor;
        this.recorrencia = recorrencia;
        this.intervaloDias = intervaloDias;
        this.observacao = observacao;
    }

    public void concluir(LocalDate hoje) {
        this.concluido = true;
        this.dataConclusao = hoje;
    }

    /** Desfaz a conclusão: o lembrete volta a ficar pendente. */
    public void reabrir() {
        this.concluido = false;
        this.dataConclusao = null;
    }

    /** Cria a próxima ocorrência pendente, copiando os dados deste lembrete. */
    public Lembrete novaOcorrencia(LocalDate novaDataVencimento, LocalDateTime agora) {
        Lembrete proxima = new Lembrete(usuario, titulo, categoria, novaDataVencimento, valor, recorrencia,
                intervaloDias, observacao, agora);
        proxima.origemId = this.id;
        return proxima;
    }

    /** RN07: pendente e com vencimento anterior a hoje. */
    public boolean isAtrasado(LocalDate hoje) {
        return !concluido && dataVencimento.isBefore(hoje);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
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

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public Long getOrigemId() {
        return origemId;
    }
}
