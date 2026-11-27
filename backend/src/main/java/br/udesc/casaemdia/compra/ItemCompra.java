package br.udesc.casaemdia.compra;

import java.time.LocalDateTime;

import br.udesc.casaemdia.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Item da lista de compras da casa.
 */
@Entity
@Table(name = "itens_compra", indexes = {
        @Index(name = "idx_itens_compra_usuario", columnList = "usuario_id")
})
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private boolean comprado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    /** Construtor exigido pela JPA. */
    protected ItemCompra() {
    }

    public ItemCompra(Usuario usuario, String nome, int quantidade, LocalDateTime criadoEm) {
        this.usuario = usuario;
        this.nome = nome;
        this.quantidade = quantidade;
        this.criadoEm = criadoEm;
    }

    public void atualizar(String nome, int quantidade) {
        this.nome = nome;
        this.quantidade = quantidade;
    }

    public void alternarComprado() {
        this.comprado = !this.comprado;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public boolean isComprado() {
        return comprado;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
