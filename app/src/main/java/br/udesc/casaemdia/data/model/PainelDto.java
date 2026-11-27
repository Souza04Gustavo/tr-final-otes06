package br.udesc.casaemdia.data.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/** Visão geral da casa devolvida por {@code GET /api/painel}. */
public class PainelDto {

    private String nomeCasa;
    private LocalDate dataReferencia;
    private List<LembreteDto> atrasados;
    private List<LembreteDto> hoje;
    private List<LembreteDto> proximos7Dias;
    private long itensPendentesCompra;
    private ResumoMesDto resumoMes;

    public String getNomeCasa() {
        return nomeCasa;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public List<LembreteDto> getAtrasados() {
        return atrasados == null ? Collections.emptyList() : atrasados;
    }

    public List<LembreteDto> getHoje() {
        return hoje == null ? Collections.emptyList() : hoje;
    }

    public List<LembreteDto> getProximos7Dias() {
        return proximos7Dias == null ? Collections.emptyList() : proximos7Dias;
    }

    public long getItensPendentesCompra() {
        return itensPendentesCompra;
    }

    public ResumoMesDto getResumoMes() {
        return resumoMes;
    }
}
