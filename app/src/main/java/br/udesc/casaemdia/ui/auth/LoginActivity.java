package br.udesc.casaemdia.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.session.SessionManager;
import br.udesc.casaemdia.databinding.ActivityLoginBinding;
import br.udesc.casaemdia.ui.main.MainActivity;
import br.udesc.casaemdia.util.Janelas;
import br.udesc.casaemdia.util.Permissoes;
import br.udesc.casaemdia.util.Resource;

/**
 * T1 – Login. É também o ponto de entrada do app: com sessão ativa, segue direto para o painel.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    private final ActivityResultLauncher<String> pedidoRedeLocal =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
                if (!concedida) {
                    Snackbar.make(binding.getRoot(), R.string.rede_local_negada, Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SessionManager.getInstance(this).estaLogado()) {
            abrirPrincipal();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Janelas.configurar(this, binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.getResultado().observe(this, this::exibir);

        binding.botaoEntrar.setOnClickListener(v -> entrar());
        binding.campoSenhaTexto.setOnEditorActionListener((v, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });
        binding.botaoCriarConta.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));

        // Android 17+: sem esta permissão o sistema bloqueia o acesso ao back-end local
        if (Permissoes.precisaRedeLocal(this)) {
            pedidoRedeLocal.launch(Permissoes.REDE_LOCAL);
        }
    }

    private void entrar() {
        boolean emailValido = ValidacaoCampos.email(binding.campoEmail);
        boolean senhaPreenchida = ValidacaoCampos.obrigatorio(binding.campoSenha);
        if (emailValido && senhaPreenchida) {
            viewModel.login(ValidacaoCampos.texto(binding.campoEmail),
                    String.valueOf(binding.campoSenhaTexto.getText()));
        }
    }

    private void exibir(Resource<?> estado) {
        boolean carregando = estado.isCarregando();
        binding.progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        binding.botaoEntrar.setEnabled(!carregando);

        if (estado.getStatus() == Resource.Status.SUCESSO) {
            abrirPrincipal();
        } else if (estado.getStatus() == Resource.Status.ERRO) {
            Snackbar.make(binding.getRoot(), String.valueOf(estado.getMensagem()), Snackbar.LENGTH_LONG).show();
        }
    }

    private void abrirPrincipal() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
