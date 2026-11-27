package br.udesc.casaemdia.ui.gastos;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.YearMonth;

import br.udesc.casaemdia.data.model.AnaliseGastosDto;
import br.udesc.casaemdia.data.repository.AnaliseRepository;
import br.udesc.casaemdia.data.repository.ResultadoCallback;
import br.udesc.casaemdia.util.Resource;

/**
 * Análise de gastos do mês escolhido (começa no mês atual).
 */
public class GastosViewModel extends ViewModel {

    /** Quantos meses o gráfico de histórico mostra, incluindo o mês analisado. */
    static final int MESES_HISTORICO = 6;

    private final AnaliseRepository repository = new AnaliseRepository();
    private final MutableLiveData<Resource<AnaliseGastosDto>> analise = new MutableLiveData<>();
    private final MutableLiveData<YearMonth> mes = new MutableLiveData<>(YearMonth.now());
    /** Número da última requisição: respostas de meses que o usuário já deixou para trás são ignoradas. */
    private int ultimaRequisicao;

    public LiveData<Resource<AnaliseGastosDto>> getAnalise() {
        return analise;
    }

    public LiveData<YearMonth> getMes() {
        return mes;
    }

    public void mesAnterior() {
        irPara(mesAtual().minusMonths(1));
    }

    public void proximoMes() {
        irPara(mesAtual().plusMonths(1));
    }

    public void irPara(YearMonth novoMes) {
        mes.setValue(novoMes);
        carregar();
    }

    public void carregar() {
        int requisicao = ++ultimaRequisicao;
        analise.setValue(Resource.carregando());
        repository.gastos(mesAtual(), MESES_HISTORICO, new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable AnaliseGastosDto dados) {
                if (requisicao == ultimaRequisicao) {
                    analise.setValue(Resource.sucesso(dados));
                }
            }

            @Override
            public void erro(String mensagem) {
                if (requisicao == ultimaRequisicao) {
                    analise.setValue(Resource.erro(mensagem));
                }
            }
        });
    }

    private YearMonth mesAtual() {
        YearMonth valor = mes.getValue();
        return valor != null ? valor : YearMonth.now();
    }
}
