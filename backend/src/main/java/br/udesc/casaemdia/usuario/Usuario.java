package br.udesc.casaemdia.usuario;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuário do app. Cada usuário representa uma casa ({@code nomeCasa}).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "nome_casa", nullable = false, length = 100)
    private String nomeCasa;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    /** Construtor exigido pela JPA. */
    protected Usuario() {
    }

    public Usuario(String nome, String nomeCasa, String email, String senhaHash, LocalDateTime criadoEm) {
        this.nome = nome;
        this.nomeCasa = nomeCasa;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeCasa() {
        return nomeCasa;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
