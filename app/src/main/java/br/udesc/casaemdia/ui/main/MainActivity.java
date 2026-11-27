package br.udesc.casaemdia.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.databinding.ActivityMainBinding;
import br.udesc.casaemdia.notificacao.AgendadorNotificacoes;
import br.udesc.casaemdia.ui.AutenticadaActivity;
import br.udesc.casaemdia.util.Janelas;
import br.udesc.casaemdia.util.Permissoes;

/**
 * Tela principal com as quatro abas (Painel, Lembretes, Gastos, Compras) e o menu superior.
 */
public class MainActivity extends AutenticadaActivity implements TelaPrincipal {

    private ActivityMainBinding binding;

    private final ActivityResultLauncher<String[]> pedidoPermissoes =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSessao()) {
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Janelas.configurar(this, binding.getRoot(), false);
        setSupportActionBar(binding.toolbar);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        if (host != null) {
            NavController navController = host.getNavController();
            NavigationUI.setupWithNavController(binding.navegacaoInferior, navController);
        }

        pedirPermissoes();
        AgendadorNotificacoes.agendarDiario(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.acao_verificar_agora) {
            AgendadorNotificacoes.verificarAgora(this);
            Snackbar.make(binding.navHost, R.string.verificando_lembretes, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.navegacaoInferior)
                    .show();
            return true;
        }
        if (item.getItemId() == R.id.acao_sair) {
            confirmarSaida();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void definirCabecalho(String titulo, @Nullable String subtitulo) {
        binding.toolbar.setTitle(titulo);
        binding.toolbar.setSubtitle(subtitulo);
    }

    @Override
    public void irParaLembretes() {
        binding.navegacaoInferior.setSelectedItemId(R.id.lembretesFragment);
    }

    @Override
    public void irParaCompras() {
        binding.navegacaoInferior.setSelectedItemId(R.id.comprasFragment);
    }

    @Override
    public void irParaGastos() {
        binding.navegacaoInferior.setSelectedItemId(R.id.gastosFragment);
    }

    private void confirmarSaida() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.sair_confirmacao)
                .setNegativeButton(R.string.acao_cancelar, null)
                .setPositiveButton(R.string.menu_sair, (dialogo, botao) -> {
                    AgendadorNotificacoes.cancelar(this);
                    sessao().encerrar();
                    irParaLogin();
                })
                .show();
    }

    /** Rede local (Android 17+) e notificações (Android 13+) são pedidas em tempo de execução. */
    private void pedirPermissoes() {
        String[] pendentes = Permissoes.pendentesDaTelaPrincipal(this);
        if (pendentes.length > 0) {
            pedidoPermissoes.launch(pendentes);
        }
    }
}
