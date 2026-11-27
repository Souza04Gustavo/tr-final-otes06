package br.udesc.casaemdia.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Permissões pedidas em tempo de execução.
 * <ul>
 * <li><b>Rede local</b> (Android 17+): sem ela o sistema bloqueia o acesso a endereços
 * privados como 10.0.2.2 ou 192.168.x.x, onde o back-end roda durante o desenvolvimento.</li>
 * <li><b>Notificações</b> (Android 13+): para os avisos de lembretes.</li>
 * </ul>
 */
public final class Permissoes {

    /** {@code Manifest.permission.ACCESS_LOCAL_NETWORK}, introduzida na API 37. */
    public static final String REDE_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK";
    private static final int API_REDE_LOCAL = 37;

    private Permissoes() {
    }

    public static boolean precisaRedeLocal(Context context) {
        return Build.VERSION.SDK_INT >= API_REDE_LOCAL && naoConcedida(context, REDE_LOCAL);
    }

    public static boolean precisaNotificacoes(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && naoConcedida(context, Manifest.permission.POST_NOTIFICATIONS);
    }

    /** Permissões que a tela principal deve pedir (pode ser vazio). */
    public static String[] pendentesDaTelaPrincipal(Context context) {
        List<String> pendentes = new ArrayList<>();
        if (precisaRedeLocal(context)) {
            pendentes.add(REDE_LOCAL);
        }
        if (precisaNotificacoes(context)) {
            pendentes.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        return pendentes.toArray(new String[0]);
    }

    private static boolean naoConcedida(Context context, String permissao) {
        return ContextCompat.checkSelfPermission(context, permissao) != PackageManager.PERMISSION_GRANTED;
    }
}
