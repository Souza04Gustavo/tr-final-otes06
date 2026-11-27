package br.udesc.casaemdia.notificacao;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.api.ApiClient;
import br.udesc.casaemdia.data.model.PainelDto;
import br.udesc.casaemdia.data.session.SessionManager;
import retrofit2.Response;

/**
 * Consulta o painel em segundo plano e notifica se houver lembretes para hoje ou atrasados.
 * Roda uma vez por dia (agendado) ou na hora, pelo menu "Verificar lembretes agora" (RF18).
 */
public class LembreteWorker extends Worker {

    /** Verificação manual: também avisa quando está tudo em dia. */
    public static final String CHAVE_MANUAL = "manual";

    public LembreteWorker(@NonNull Context context, @NonNull WorkerParameters parametros) {
        super(context, parametros);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionManager sessao = SessionManager.getInstance(context);
        if (!sessao.estaLogado()) {
            return Result.success();
        }
        ApiClient.inicializar(sessao);
        boolean manual = getInputData().getBoolean(CHAVE_MANUAL, false);

        try {
            Response<PainelDto> resposta = ApiClient.painel().painel().execute();
            PainelDto painel = resposta.body();
            if (!resposta.isSuccessful() || painel == null) {
                return Result.failure();
            }

            int paraHoje = painel.getHoje().size();
            int atrasados = painel.getAtrasados().size();
            if (paraHoje + atrasados > 0) {
                NotificacaoHelper.mostrar(context, NotificacaoHelper.montarTexto(context, paraHoje, atrasados));
            } else if (manual) {
                NotificacaoHelper.mostrar(context, context.getString(R.string.notificacao_tudo_em_dia));
            }
            return Result.success();
        } catch (IOException e) {
            // Sem conexão: a verificação diária tenta de novo mais tarde
            return manual ? Result.failure() : Result.retry();
        }
    }
}
