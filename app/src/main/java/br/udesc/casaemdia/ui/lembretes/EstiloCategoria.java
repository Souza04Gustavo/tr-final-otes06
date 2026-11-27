package br.udesc.casaemdia.ui.lembretes;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.Categoria;

/**
 * Cor e nomes de cada categoria (azul = conta, âmbar = manutenção, verde-azulado = outro).
 * As três cores foram validadas para daltonismo (protan/deutan) e contraste, pois também aparecem nos gráficos.
 */
public enum EstiloCategoria {

    CONTA(R.color.conta, R.string.categoria_conta, R.string.filtro_contas),
    MANUTENCAO(R.color.manutencao, R.string.categoria_manutencao, R.string.filtro_manutencao),
    OUTRO(R.color.outro, R.string.categoria_outro, R.string.filtro_outros);

    /** Cor do filete do item e da categoria nos gráficos. */
    @ColorRes
    public final int cor;
    /** Nome no singular ("Conta"), exibido em caixa alta no item de lembrete. */
    @StringRes
    public final int rotulo;
    /** Nome no plural ("Contas", "Manutenção", "Outros"), usado nos filtros e na análise de gastos. */
    @StringRes
    public final int nome;

    EstiloCategoria(int cor, int rotulo, int nome) {
        this.cor = cor;
        this.rotulo = rotulo;
        this.nome = nome;
    }

    public static EstiloCategoria de(Categoria categoria) {
        if (categoria == Categoria.CONTA) {
            return CONTA;
        }
        if (categoria == Categoria.MANUTENCAO) {
            return MANUTENCAO;
        }
        return OUTRO;
    }
}
