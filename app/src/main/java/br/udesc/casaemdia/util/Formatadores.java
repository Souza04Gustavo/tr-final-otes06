package br.udesc.casaemdia.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import androidx.annotation.Nullable;

/**
 * Formatação de valores e datas no padrão brasileiro.
 */
public final class Formatadores {

    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM", PT_BR);
    private static final DateTimeFormatter DATA_COMPLETA = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DateTimeFormatter DIA_SEMANA_CURTO = DateTimeFormatter.ofPattern("EEE", PT_BR);
    private static final DateTimeFormatter DIA_SEMANA = DateTimeFormatter.ofPattern("EEEE", PT_BR);
    private static final DateTimeFormatter DIA_DE_MES = DateTimeFormatter.ofPattern("d 'de' MMMM", PT_BR);
    private static final DateTimeFormatter NOME_MES = DateTimeFormatter.ofPattern("MMMM", PT_BR);
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT_BR);

    private Formatadores() {
    }

    /** 1234.5 → "R$ 1.234,50" */
    public static String moeda(@Nullable BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(PT_BR);
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }

    /** 1234.5 → "1234,50" (para preencher o campo de edição). */
    public static String valorParaCampo(@Nullable BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    /**
     * Converte o texto digitado em valor. Aceita "184,50", "184.50" e "1.234,50".
     *
     * @return o valor, ou {@code null} se o texto estiver vazio ou inválido
     */
    @Nullable
    public static BigDecimal lerValor(@Nullable String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.replace("R$", "").replace(" ", "").replace(" ", "").trim();
        if (limpo.isEmpty()) {
            return null;
        }
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(',', '.');
        }
        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** "28/11" */
    public static String diaMes(LocalDate data) {
        return data.format(DIA_MES);
    }

    /** "28/11/2026" */
    public static String dataCompleta(LocalDate data) {
        return data.format(DATA_COMPLETA);
    }

    /** "Sáb, 28/11" */
    public static String diaSemanaEData(LocalDate data) {
        String dia = data.format(DIA_SEMANA_CURTO).replace(".", "");
        return capitalizar(dia) + ", " + diaMes(data);
    }

    /** "Quinta, 26 de novembro" */
    public static String dataPorExtenso(LocalDate data) {
        String diaSemana = data.format(DIA_SEMANA).split("-")[0];
        return capitalizar(diaSemana) + ", " + data.format(DIA_DE_MES);
    }

    /** "2026-11" → "novembro" (ou o próprio texto, se não for um mês válido). */
    public static String nomeDoMes(@Nullable String anoMes) {
        if (anoMes == null) {
            return "";
        }
        try {
            return YearMonth.parse(anoMes).format(NOME_MES);
        } catch (DateTimeParseException e) {
            return anoMes;
        }
    }

    /** 2026-11 → "Novembro de 2026" */
    public static String mesAno(YearMonth mes) {
        return capitalizar(mes.format(MES_ANO));
    }

    /** 2026-11 → "nov" (rótulo curto dos gráficos, sem ponto de abreviação). */
    public static String mesCurto(YearMonth mes) {
        String nome = mes.format(NOME_MES);
        return nome.length() <= 3 ? nome : nome.substring(0, 3);
    }

    /** 41.7 → "41,7"; -12 → "12"; 5.0 → "5" (o sinal é mostrado pela seta, não pelo número). */
    public static String percentual(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(PT_BR);
        formato.setMaximumFractionDigits(1);
        return formato.format(valor.abs());
    }

    private static String capitalizar(String texto) {
        if (texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(PT_BR) + texto.substring(1);
    }
}
