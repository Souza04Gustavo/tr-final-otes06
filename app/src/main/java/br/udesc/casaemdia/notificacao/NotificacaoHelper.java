package br.udesc.casaemdia.notificacao;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.ui.auth.LoginActivity;

/**
 * Cria o canal e exibe a notificação de lembretes da casa (RF17).
 */
public final class NotificacaoHelper {

    public static final String CANAL_LEMBRETES = "lembretes";
    private static final int ID_NOTIFICACAO = 1001;

    private NotificacaoHelper() {
    }

    public static void criarCanal(Context context) {
        NotificationChannel canal = new NotificationChannel(
                CANAL_LEMBRETES,
                context.getString(R.string.canal_lembretes_nome),
                NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription(context.getString(R.string.canal_lembretes_descricao));
        context.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    /** "1 lembrete para hoje, 2 atrasados" */
    public static String montarTexto(Context context, int paraHoje, int atrasados) {
        List<String> partes = new ArrayList<>();
        if (paraHoje > 0) {
            partes.add(context.getResources().getQuantityString(R.plurals.notificacao_hoje, paraHoje, paraHoje));
        }
        if (atrasados > 0) {
            partes.add(context.getResources().getQuantityString(R.plurals.notificacao_atrasados, atrasados, atrasados));
        }
        return String.join(", ", partes);
    }

    @SuppressLint("MissingPermission") // a permissão é verificada em podeNotificar()
    public static void mostrar(Context context, String texto) {
        if (!podeNotificar(context)) {
            return;
        }

        Intent abrirApp = new Intent(context, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent aoTocar = PendingIntent.getActivity(context, 0, abrirApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(context, CANAL_LEMBRETES)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setColor(ContextCompat.getColor(context, R.color.primaria))
                .setContentTitle(context.getString(R.string.notificacao_titulo))
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setContentIntent(aoTocar)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(ID_NOTIFICACAO, notificacao.build());
    }

    private static boolean podeNotificar(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
}
