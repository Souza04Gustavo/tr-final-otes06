package br.udesc.casaemdia.ui;

import android.content.Context;

import androidx.annotation.Nullable;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.Recorrencia;

/**
 * Textos exibidos para os enums da API.
 */
public final class Rotulos {

    private Rotulos() {
    }

    /** Rótulo curto usado nas listas: "Mensal", "A cada 180 dias"... */
    public static String recorrenciaCurta(Context context, Recorrencia recorrencia, @Nullable Integer intervaloDias) {
        switch (recorrencia) {
            case SEMANAL:
                return context.getString(R.string.recorrencia_curta_semanal);
            case MENSAL:
                return context.getString(R.string.recorrencia_curta_mensal);
            case ANUAL:
                return context.getString(R.string.recorrencia_curta_anual);
            case A_CADA_N_DIAS:
                int dias = intervaloDias == null ? 0 : intervaloDias;
                return context.getResources().getQuantityString(
                        R.plurals.recorrencia_curta_a_cada_n_dias, dias, dias);
            case NENHUMA:
            default:
                return context.getString(R.string.recorrencia_curta_nenhuma);
        }
    }

    /** Opções do campo "Repetir", na mesma ordem de {@link Recorrencia#values()}. */
    public static String[] recorrenciasDoFormulario(Context context) {
        return new String[] {
                context.getString(R.string.recorrencia_nenhuma),
                context.getString(R.string.recorrencia_semanal),
                context.getString(R.string.recorrencia_mensal),
                context.getString(R.string.recorrencia_anual),
                context.getString(R.string.recorrencia_a_cada_n_dias)
        };
    }
}
