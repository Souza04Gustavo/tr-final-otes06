package br.udesc.casaemdia.data.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Resposta de {@code GET /api/analises/gastos}. Os meses vêm no formato yyyy-MM.
 */
public class AnaliseGastosDto {

    private String mes;
    private BigDecimal total;
    private BigDecimal pago;
    private BigDecimal pendente;
    private int quantidadeLancamentos;
    private BigDecimal mediaMensal;
    /** {@code null} quando o mês anterior não teve gasto. */
    private BigDecimal variacaoPercentual;
    private List<GastoMensal> historico;
    private List<GastoCategoria> porCategoria;
    private List<MaiorGasto> maioresGastos;

    public String getMes() {
        return mes;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPago() {
        return pago;
    }

    public BigDecimal getPendente() {
        return pendente;
    }

    public int getQuantidadeLancamentos() {
        return quantidadeLancamentos;
    }

    public BigDecimal getMediaMensal() {
        return mediaMensal;
    }

    public BigDecimal getVariacaoPercentual() {
        return variacaoPercentual;
    }

    public List<GastoMensal> getHistorico() {
        return historico == null ? Collections.emptyList() : historico;
    }

    public List<GastoCategoria> getPorCategoria() {
        return porCategoria == null ? Collections.emptyList() : porCategoria;
    }

    public List<MaiorGasto> getMaioresGastos() {
        return maioresGastos == null ? Collections.emptyList() : maioresGastos;
    }

    /** Total de um mês do histórico. */
    public static class GastoMensal {
        private String mes;
        private BigDecimal total;
        private BigDecimal pago;

        public String getMes() {
            return mes;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public BigDecimal getPago() {
            return pago;
        }
    }

    /** Fatia do mês que pertence a uma categoria. */
    public static class GastoCategoria {
        private Categoria categoria;
        private BigDecimal valor;
        private int percentual;

        public Categoria getCategoria() {
            return categoria;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public int getPercentual() {
            return percentual;
        }
    }

    /** Um dos maiores gastos do mês; {@code agrupado} marca a linha "Outros". */
    public static class MaiorGasto {
        private String titulo;
        private BigDecimal valor;
        private int percentual;
        private boolean agrupado;

        public String getTitulo() {
            return titulo;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public int getPercentual() {
            return percentual;
        }

        public boolean isAgrupado() {
            return agrupado;
        }
    }
}
