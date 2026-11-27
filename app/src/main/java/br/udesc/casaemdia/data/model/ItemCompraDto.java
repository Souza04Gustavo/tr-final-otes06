package br.udesc.casaemdia.data.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ItemCompraDto {

    private Long id;
    private String nome;
    private int quantidade;
    private boolean comprado;
    private LocalDateTime criadoEm;

    public Long getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemCompraDto)) {
            return false;
        }
        ItemCompraDto outro = (ItemCompraDto) o;
        return quantidade == outro.quantidade
                && comprado == outro.comprado
                && Objects.equals(id, outro.id)
                && Objects.equals(nome, outro.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, quantidade, comprado);
    }
}
