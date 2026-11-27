package br.udesc.casaemdia.ui;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.session.SessionManager;
import br.udesc.casaemdia.ui.auth.LoginActivity;

/**
 * Base das telas que exigem login: volta para o Login se não houver sessão
 * ou se a API informar que o token expirou (HTTP 401).
 */
public abstract class AutenticadaActivity extends AppCompatActivity {

    protected SessionManager sessao() {
        return SessionManager.getInstance(this);
    }

    /**
     * Deve ser chamado logo após {@code super.onCreate()}.
     *
     * @return {@code false} se não há sessão (a tela já foi encerrada)
     */
    protected boolean exigirSessao() {
        if (!sessao().estaLogado()) {
            irParaLogin();
            return false;
        }
        sessao().getSessaoExpirada().observe(this, evento -> {
            if (evento.consumir() != null) {
                Toast.makeText(this, R.string.sessao_expirada, Toast.LENGTH_LONG).show();
                irParaLogin();
            }
        });
        return true;
    }

    protected void irParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
