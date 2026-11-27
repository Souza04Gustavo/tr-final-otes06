package br.udesc.casaemdia.data.model;

public class CadastroRequest {

    private final String nome;
    private final String nomeCasa;
    private final String email;
    private final String senha;

    public CadastroRequest(String nome, String nomeCasa, String email, String senha) {
        this.nome = nome;
        this.nomeCasa = nomeCasa;
        this.email = email;
        this.senha = senha;
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

    public String getSenha() {
        return senha;
    }
}
