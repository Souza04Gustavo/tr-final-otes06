package br.udesc.casaemdia.util;

import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Configura o modo "edge-to-edge" (obrigatório a partir do Android 15) aplicando
 * as margens das barras do sistema e do teclado na view raiz da tela.
 */
public final class Janelas {

    private Janelas() {
    }

    /** Aplica margens em todos os lados (telas sem barra de navegação inferior própria). */
    public static void configurar(ComponentActivity activity, View raiz) {
        configurar(activity, raiz, true);
    }

    /**
     * @param incluirInferior {@code false} quando a tela tem um BottomNavigationView,
     *                        que já trata a margem inferior sozinho
     */
    public static void configurar(ComponentActivity activity, View raiz, boolean incluirInferior) {
        EdgeToEdge.enable(activity);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            int inferior = incluirInferior ? Math.max(barras.bottom, teclado.bottom) : 0;
            view.setPadding(barras.left, barras.top, barras.right, inferior);
            return incluirInferior ? WindowInsetsCompat.CONSUMED : insets;
        });
    }
}
