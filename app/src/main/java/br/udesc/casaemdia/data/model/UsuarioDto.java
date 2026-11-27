package br.udesc.casaemdia.data.model;

public class UsuarioDto {

    private Long id;
    private String nome;
    private String nomeCasa;
    private String email;

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
}
