package br.udesc.casaemdia.notificacao;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Agenda a verificação de lembretes com o WorkManager.
 */
public final class AgendadorNotificacoes {

    private static final String TRABALHO_DIARIO = "verificacao-diaria-lembretes";
    private static final String TRABALHO_MANUAL = "verificacao-manual-lembretes";
    private static final LocalTime HORARIO_DIARIO = LocalTime.of(8, 0);

    private AgendadorNotificacoes() {
    }

    /** Agenda (uma única vez) a verificação diária, por volta das 8h. */
    public static void agendarDiario(Context context) {
        PeriodicWorkRequest requisicao = new PeriodicWorkRequest.Builder(LembreteWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(comRede())
                .setInitialDelay(atrasoAteProximoHorario().toMinutes(), TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TRABALHO_DIARIO, ExistingPeriodicWorkPolicy.KEEP, requisicao);
    }

    /** Executa a verificação imediatamente (útil para a demonstração). */
    public static void verificarAgora(Context context) {
        OneTimeWorkRequest requisicao = new OneTimeWorkRequest.Builder(LembreteWorker.class)
                .setConstraints(comRede())
                .setInputData(new Data.Builder().putBoolean(LembreteWorker.CHAVE_MANUAL, true).build())
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(TRABALHO_MANUAL, ExistingWorkPolicy.REPLACE, requisicao);
    }

    /** Chamado no logout. */
    public static void cancelar(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(TRABALHO_DIARIO);
    }

    private static Constraints comRede() {
        return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    }

    private static Duration atrasoAteProximoHorario() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime proximo = agora.toLocalDate().atTime(HORARIO_DIARIO);
        if (!proximo.isAfter(agora)) {
            proximo = proximo.plusDays(1);
        }
        return Duration.between(agora, proximo);
    }
}
