package br.udesc.casaemdia.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Testes unitários (JVM) da formatação e leitura de valores e datas no padrão brasileiro.
 */
public class FormatadoresTest {

    @Test
    public void lerValor_aceitaVirgulaComoSeparadorDecimal() {
        assertEquals(new BigDecimal("184.50"), Formatadores.lerValor("184,50"));
    }

    @Test
    public void lerValor_aceitaPontoComoSeparadorDecimal() {
        assertEquals(new BigDecimal("184.50"), Formatadores.lerValor("184.50"));
    }

    @Test
    public void lerValor_aceitaSeparadorDeMilhar() {
        assertEquals(new BigDecimal("1234.50"), Formatadores.lerValor("1.234,50"));
    }

    @Test
    public void lerValor_ignoraPrefixoDeMoeda() {
        assertEquals(new BigDecimal("99.90"), Formatadores.lerValor("R$ 99,90"));
    }

    @Test
    public void lerValor_textoVazioOuInvalidoRetornaNulo() {
        assertNull(Formatadores.lerValor(""));
        assertNull(Formatadores.lerValor("   "));
        assertNull(Formatadores.lerValor("abc"));
        assertNull(Formatadores.lerValor(null));
    }

    @Test
    public void valorParaCampo_usaVirgulaEDuasCasas() {
        assertEquals("184,50", Formatadores.valorParaCampo(new BigDecimal("184.5")));
        assertEquals("", Formatadores.valorParaCampo(null));
    }

    @Test
    public void moeda_formataNoPadraoBrasileiro() {
        // O separador entre "R$" e o número pode ser um espaço não separável
        String formatado = Formatadores.moeda(new BigDecimal("1570.70")).replace(' ', ' ');
        assertEquals("R$ 1.570,70", formatado);
    }

    @Test
    public void datas_formatadasNoPadraoBrasileiro() {
        LocalDate data = LocalDate.of(2026, 11, 28);
        assertEquals("28/11", Formatadores.diaMes(data));
        assertEquals("28/11/2026", Formatadores.dataCompleta(data));
        assertEquals("Sáb, 28/11", Formatadores.diaSemanaEData(data));
    }

    @Test
    public void dataPorExtenso_usaODiaDaSemanaSemFeira() {
        assertEquals("Quinta, 26 de novembro", Formatadores.dataPorExtenso(LocalDate.of(2026, 11, 26)));
    }

    @Test
    public void mesAno_eMesCurto_paraOsGraficos() {
        assertEquals("Novembro de 2026", Formatadores.mesAno(YearMonth.of(2026, 11)));
        assertEquals("nov", Formatadores.mesCurto(YearMonth.of(2026, 11)));
        assertEquals("mai", Formatadores.mesCurto(YearMonth.of(2026, 5)));
    }

    @Test
    public void percentual_usaVirgulaEOmiteOSinal() {
        assertEquals("41,7", Formatadores.percentual(new BigDecimal("41.7")));
        assertEquals("12", Formatadores.percentual(new BigDecimal("-12.0")));
    }

    @Test
    public void nomeDoMes_converteAnoMes() {
        assertEquals("novembro", Formatadores.nomeDoMes("2026-11"));
        assertEquals("invalido", Formatadores.nomeDoMes("invalido"));
    }
}
