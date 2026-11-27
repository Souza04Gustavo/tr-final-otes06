package br.udesc.casaemdia.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import br.udesc.casaemdia.databinding.ActivityCadastroBinding;
import br.udesc.casaemdia.ui.main.MainActivity;
import br.udesc.casaemdia.util.Janelas;
import br.udesc.casaemdia.util.Resource;

/**
 * T2 – Cadastro. Após criar a conta, o usuário já entra no app (a API devolve o token).
 */
public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Janelas.configurar(this, binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.botaoEntrar.setOnClickListener(v -> finish());
        binding.botaoCadastrar.setOnClickListener(v -> cadastrar());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.getResultado().observe(this, this::exibir);
    }

    private void cadastrar() {
        boolean nome = ValidacaoCampos.obrigatorio(binding.campoNome);
        boolean nomeCasa = ValidacaoCampos.obrigatorio(binding.campoNomeCasa);
        boolean email = ValidacaoCampos.email(binding.campoEmail);
        boolean senha = ValidacaoCampos.senha(binding.campoSenha);
        if (nome && nomeCasa && email && senha) {
            viewModel.cadastrar(
                    ValidacaoCampos.texto(binding.campoNome),
                    ValidacaoCampos.texto(binding.campoNomeCasa),
                    ValidacaoCampos.texto(binding.campoEmail),
                    String.valueOf(binding.campoSenhaTexto.getText()));
        }
    }

    private void exibir(Resource<?> estado) {
        boolean carregando = estado.isCarregando();
        binding.progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        binding.botaoCadastrar.setEnabled(!carregando);

        if (estado.getStatus() == Resource.Status.SUCESSO) {
            startActivity(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else if (estado.getStatus() == Resource.Status.ERRO) {
            Snackbar.make(binding.getRoot(), String.valueOf(estado.getMensagem()), Snackbar.LENGTH_LONG).show();
        }
    }
}
