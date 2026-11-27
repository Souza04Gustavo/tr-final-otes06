package br.udesc.casaemdia;

import android.app.Application;

import br.udesc.casaemdia.data.api.ApiClient;
import br.udesc.casaemdia.data.session.SessionManager;
import br.udesc.casaemdia.notificacao.NotificacaoHelper;

/**
 * Inicializa, uma única vez por processo, o cliente da API e o canal de notificações.
 */
public class CasaEmDiaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.inicializar(SessionManager.getInstance(this));
        NotificacaoHelper.criarCanal(this);
    }
}
