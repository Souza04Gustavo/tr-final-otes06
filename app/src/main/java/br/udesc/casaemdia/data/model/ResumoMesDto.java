package br.udesc.casaemdia.data.model;

import java.math.BigDecimal;

/** Resumo das contas do mês; {@code mes} vem no formato yyyy-MM. */
public class ResumoMesDto {

    private String mes;
    private BigDecimal totalContas;
    private BigDecimal totalPago;
    private BigDecimal totalPendente;

    public String getMes() {
        return mes;
    }

    public BigDecimal getTotalContas() {
        return totalContas;
    }

    public BigDecimal getTotalPago() {
        return totalPago;
    }

    public BigDecimal getTotalPendente() {
        return totalPendente;
    }
}
