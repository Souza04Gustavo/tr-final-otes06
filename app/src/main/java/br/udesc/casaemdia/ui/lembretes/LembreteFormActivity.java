package br.udesc.casaemdia.ui.lembretes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.Categoria;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.data.model.LembreteRequest;
import br.udesc.casaemdia.data.model.Recorrencia;
import br.udesc.casaemdia.databinding.ActivityLembreteFormBinding;
import br.udesc.casaemdia.ui.AutenticadaActivity;
import br.udesc.casaemdia.ui.Rotulos;
import br.udesc.casaemdia.util.Formatadores;
import br.udesc.casaemdia.util.Janelas;
import br.udesc.casaemdia.util.Resource;

/**
 * T5 – Formulário para criar ou editar um lembrete.
 */
public class LembreteFormActivity extends AutenticadaActivity {

    private static final String EXTRA_ID = "lembrete_id";
    private static final String ESTADO_DATA = "data";
    private static final String ESTADO_RECORRENCIA = "recorrencia";
    private static final int INTERVALO_MAXIMO = 3650;

    private ActivityLembreteFormBinding binding;
    private LembreteFormViewModel viewModel;

    @Nullable
    private Long id;
    private LocalDate data = LocalDate.now();
    private Recorrencia recorrencia = Recorrencia.NENHUMA;
    private String[] opcoesRecorrencia;

    public static Intent intentNovo(Context context) {
        return new Intent(context, LembreteFormActivity.class);
    }

    public static Intent intentEditar(Context context, long id) {
        return new Intent(context, LembreteFormActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSessao()) {
            return;
        }
        binding = ActivityLembreteFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Janelas.configurar(this, binding.getRoot());

        id = getIntent().hasExtra(EXTRA_ID) ? getIntent().getLongExtra(EXTRA_ID, 0) : null;
        if (savedInstanceState != null) {
            data = LocalDate.parse(savedInstanceState.getString(ESTADO_DATA, LocalDate.now().toString()));
            recorrencia = Recorrencia.valueOf(savedInstanceState.getString(ESTADO_RECORRENCIA, Recorrencia.NENHUMA.name()));
        }

        configurarCampos();

        viewModel = new ViewModelProvider(this).get(LembreteFormViewModel.class);
        viewModel.getOperacao().observe(this, this::exibirOperacao);
        if (id != null) {
            viewModel.getLembrete().observe(this, this::exibirLembrete);
            viewModel.carregar(id);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putString(ESTADO_DATA, data.toString());
        estado.putString(ESTADO_RECORRENCIA, recorrencia.name());
    }

    private void configurarCampos() {
        boolean edicao = id != null;
        binding.toolbar.setTitle(edicao ? R.string.form_titulo_editar : R.string.form_titulo_novo);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.botaoExcluir.setVisibility(edicao ? View.VISIBLE : View.GONE);
        binding.botaoExcluir.setOnClickListener(v -> confirmarExclusao());
        binding.botaoSalvar.setOnClickListener(v -> salvar());

        // Categoria: "Valor" passa a ser obrigatório em contas
        binding.grupoCategoria.addOnButtonCheckedListener((grupo, botao, marcado) -> {
            if (marcado) {
                atualizarRotuloValor();
            }
        });
        atualizarRotuloValor();

        // Data: abre o calendário
        binding.campoDataTexto.setOnClickListener(v -> abrirCalendario());
        binding.campoData.setEndIconOnClickListener(v -> abrirCalendario());
        exibirData();

        // Recorrência: o campo "a cada N dias" só aparece quando faz sentido
        opcoesRecorrencia = Rotulos.recorrenciasDoFormulario(this);
        binding.campoRecorrenciaTexto.setSimpleItems(opcoesRecorrencia);
        binding.campoRecorrenciaTexto.setOnItemClickListener((pai, view, posicao, idItem) ->
                selecionarRecorrencia(Recorrencia.values()[posicao]));
        selecionarRecorrencia(recorrencia);
    }

    private void exibirLembrete(Resource<LembreteDto> estado) {
        boolean carregando = estado.isCarregando();
        binding.progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        binding.botaoSalvar.setEnabled(!carregando);

        if (estado.getStatus() == Resource.Status.ERRO) {
            Toast.makeText(this, estado.getMensagem(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        LembreteDto lembrete = estado.getDados();
        if (estado.getStatus() != Resource.Status.SUCESSO || lembrete == null
                || !viewModel.devePreencherFormulario()) {
            return;
        }

        binding.campoTituloTexto.setText(lembrete.getTitulo());
        binding.grupoCategoria.check(botaoDaCategoria(lembrete.getCategoria()));
        data = lembrete.getDataVencimento();
        exibirData();
        binding.campoValorTexto.setText(Formatadores.valorParaCampo(lembrete.getValor()));
        selecionarRecorrencia(lembrete.getRecorrencia());
        binding.campoIntervaloTexto.setText(lembrete.getIntervaloDias() == null
                ? "" : String.valueOf(lembrete.getIntervaloDias()));
        binding.campoObservacaoTexto.setText(lembrete.getObservacao());
    }

    private void exibirOperacao(Resource<String> estado) {
        boolean carregando = estado.isCarregando();
        binding.progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        binding.botaoSalvar.setEnabled(!carregando);
        binding.botaoExcluir.setEnabled(!carregando);

        if (estado.getStatus() == Resource.Status.SUCESSO) {
            Toast.makeText(this, estado.getDados(), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else if (estado.getStatus() == Resource.Status.ERRO) {
            Snackbar.make(binding.getRoot(), String.valueOf(estado.getMensagem()), Snackbar.LENGTH_LONG).show();
        }
    }

    /** Valida os campos (mesmas regras do back-end) e envia. */
    private void salvar() {
        String titulo = texto(binding.campoTitulo);
        Categoria categoria = categoriaSelecionada();
        String textoValor = texto(binding.campoValor);
        BigDecimal valor = Formatadores.lerValor(textoValor);
        Integer intervalo = null;
        boolean valido = true;

        limparErros();
        if (titulo.isEmpty()) {
            binding.campoTitulo.setError(getString(R.string.erro_campo_obrigatorio));
            valido = false;
        }
        if (!textoValor.isEmpty() && (valor == null || valor.signum() <= 0)) {
            binding.campoValor.setError(getString(R.string.erro_valor_invalido));
            valido = false;
        } else if (categoria == Categoria.CONTA && valor == null) {
            binding.campoValor.setError(getString(R.string.erro_valor_obrigatorio));
            valido = false;
        }
        if (recorrencia == Recorrencia.A_CADA_N_DIAS) {
            intervalo = lerInteiro(texto(binding.campoIntervalo));
            if (intervalo == null || intervalo < 1 || intervalo > INTERVALO_MAXIMO) {
                binding.campoIntervalo.setError(getString(R.string.erro_intervalo));
                valido = false;
            }
        }
        if (!valido) {
            return;
        }

        String observacao = texto(binding.campoObservacao);
        viewModel.salvar(id, new LembreteRequest(titulo, categoria, data, valor, recorrencia, intervalo,
                observacao.isEmpty() ? null : observacao));
    }

    private void confirmarExclusao() {
        if (id == null) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.excluir_lembrete_confirmacao)
                .setNegativeButton(R.string.acao_cancelar, null)
                .setPositiveButton(R.string.acao_excluir, (dialogo, botao) -> viewModel.excluir(id))
                .show();
    }

    private void abrirCalendario() {
        // O MaterialDatePicker trabalha com milissegundos em UTC
        MaterialDatePicker<Long> calendario = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.selecionar_data)
                .setSelection(data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                .build();
        calendario.addOnPositiveButtonClickListener(selecao -> {
            data = Instant.ofEpochMilli(selecao).atZone(ZoneOffset.UTC).toLocalDate();
            exibirData();
        });
        calendario.show(getSupportFragmentManager(), "calendario");
    }

    private void selecionarRecorrencia(Recorrencia nova) {
        recorrencia = nova;
        binding.campoRecorrenciaTexto.setText(opcoesRecorrencia[nova.ordinal()], false);
        boolean aCadaNDias = nova == Recorrencia.A_CADA_N_DIAS;
        binding.campoIntervalo.setVisibility(aCadaNDias ? View.VISIBLE : View.GONE);
        binding.ajudaIntervalo.setVisibility(aCadaNDias ? View.VISIBLE : View.GONE);
    }

    private void atualizarRotuloValor() {
        binding.campoValor.setHint(categoriaSelecionada() == Categoria.CONTA
                ? R.string.campo_valor : R.string.campo_valor_opcional);
    }

    private void exibirData() {
        binding.campoDataTexto.setText(Formatadores.dataCompleta(data));
    }

    private Categoria categoriaSelecionada() {
        int botao = binding.grupoCategoria.getCheckedButtonId();
        if (botao == R.id.botao_manutencao) {
            return Categoria.MANUTENCAO;
        }
        if (botao == R.id.botao_outro) {
            return Categoria.OUTRO;
        }
        return Categoria.CONTA;
    }

    private static int botaoDaCategoria(Categoria categoria) {
        if (categoria == Categoria.MANUTENCAO) {
            return R.id.botao_manutencao;
        }
        if (categoria == Categoria.OUTRO) {
            return R.id.botao_outro;
        }
        return R.id.botao_conta;
    }

    private void limparErros() {
        binding.campoTitulo.setError(null);
        binding.campoValor.setError(null);
        binding.campoIntervalo.setError(null);
    }

    private static String texto(TextInputLayout campo) {
        return campo.getEditText() == null || campo.getEditText().getText() == null
                ? "" : campo.getEditText().getText().toString().trim();
    }

    @Nullable
    private static Integer lerInteiro(String texto) {
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
