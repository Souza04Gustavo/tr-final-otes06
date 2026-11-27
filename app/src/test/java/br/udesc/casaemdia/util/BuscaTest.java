package br.udesc.casaemdia.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Testes unitários (JVM) da busca de lembretes.
 */
public class BuscaTest {

    @Test
    public void normalizar_removeAcentosMaiusculasEEspacosExtras() {
        assertEquals("conta de agua", Busca.normalizar("  Conta   de ÁGUA "));
        assertEquals("", Busca.normalizar(null));
    }

    @Test
    public void corresponde_ignoraAcentosEMaiusculas() {
        assertTrue(Busca.corresponde("agua", "Conta de água"));
        assertTrue(Busca.corresponde("MANUTENÇÃO", "manutencao do portão"));
    }

    @Test
    public void corresponde_procuraEmQualquerCampo() {
        assertTrue(Busca.corresponde("enel", "Conta de luz", "Pagar no app da Enel"));
        assertFalse(Busca.corresponde("gás", "Conta de luz", null));
    }

    @Test
    public void corresponde_termoVazioAceitaTudo() {
        assertTrue(Busca.corresponde("", "Qualquer coisa"));
        assertTrue(Busca.corresponde("   ", "Qualquer coisa"));
        assertTrue(Busca.corresponde(null, (String) null));
    }
}
