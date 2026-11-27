package br.udesc.casaemdia.ui.lembretes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import java.util.List;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.Categoria;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.databinding.FragmentLembretesBinding;
import br.udesc.casaemdia.ui.Avisos;
import br.udesc.casaemdia.ui.main.TelaPrincipal;
import br.udesc.casaemdia.util.Resource;

/**
 * T4 – Lembretes: busca, filtros por categoria e situação, concluir (com "Desfazer") e criar.
 */
public class LembretesFragment extends Fragment implements LembreteAdapter.Acoes {

    private FragmentLembretesBinding binding;
    private LembretesViewModel viewModel;
    private LembreteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLembretesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new LembreteAdapter(false, this);
        binding.lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.lista.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(LembretesViewModel.class);
        viewModel.getLembretes().observe(getViewLifecycleOwner(), this::exibir);
        viewModel.getAvisos().observe(getViewLifecycleOwner(), evento -> Avisos.mostrar(binding.getRoot(), evento));

        binding.campoBuscaTexto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence texto, int inicio, int quantidade, int depois) {
            }

            @Override
            public void onTextChanged(CharSequence texto, int inicio, int antes, int quantidade) {
                viewModel.buscar(texto.toString());
            }

            @Override
            public void afterTextChanged(Editable texto) {
            }
        });
        binding.campoBuscaTexto.setOnEditorActionListener((campo, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_SEARCH) {
                esconderTeclado(campo);
                return true;
            }
            return false;
        });

        binding.chipsCategoria.setOnCheckedStateChangeListener((grupo, ids) -> aplicarFiltros());
        binding.grupoSituacao.addOnButtonCheckedListener((grupo, id, marcado) -> {
            if (marcado) {
                aplicarFiltros();
            }
        });
        binding.atualizar.setOnRefreshListener(viewModel::carregar);
        binding.botaoNovo.setOnClickListener(v ->
                startActivity(LembreteFormActivity.intentNovo(requireContext())));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TelaPrincipal) requireActivity()).definirCabecalho(getString(R.string.menu_lembretes), null);
        aplicarFiltros();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void abrir(LembreteDto lembrete) {
        startActivity(LembreteFormActivity.intentEditar(requireContext(), lembrete.getId()));
    }

    @Override
    public void concluir(LembreteDto lembrete) {
        viewModel.concluir(lembrete);
    }

    private void aplicarFiltros() {
        boolean concluidos = binding.grupoSituacao.getCheckedButtonId() == R.id.botao_concluidos;
        viewModel.filtrar(categoriaSelecionada(), concluidos);
    }

    @Nullable
    private Categoria categoriaSelecionada() {
        int chip = binding.chipsCategoria.getCheckedChipId();
        if (chip == R.id.chip_contas) {
            return Categoria.CONTA;
        }
        if (chip == R.id.chip_manutencao) {
            return Categoria.MANUTENCAO;
        }
        if (chip == R.id.chip_outros) {
            return Categoria.OUTRO;
        }
        return null;
    }

    private void exibir(Resource<List<LembreteDto>> estado) {
        binding.atualizar.setRefreshing(estado.isCarregando());
        if (estado.getStatus() == Resource.Status.ERRO) {
            Avisos.criar(binding.getRoot(), String.valueOf(estado.getMensagem()))
                    .setAction(R.string.acao_tentar_novamente, v -> viewModel.carregar())
                    .show();
            return;
        }
        if (estado.getStatus() != Resource.Status.SUCESSO) {
            return;
        }
        List<LembreteDto> lista = estado.getDados();
        adapter.submitList(lista);

        boolean vazio = lista == null || lista.isEmpty();
        binding.vazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        String busca = viewModel.getBusca().trim();
        if (!busca.isEmpty()) {
            binding.vazio.setText(getString(R.string.busca_sem_resultado, busca));
        } else {
            binding.vazio.setText(viewModel.isConcluidos()
                    ? R.string.lembretes_vazio_concluidos
                    : R.string.lembretes_vazio_pendentes);
        }
    }

    private void esconderTeclado(View campo) {
        InputMethodManager teclado = requireContext().getSystemService(InputMethodManager.class);
        teclado.hideSoftInputFromWindow(campo.getWindowToken(), 0);
        campo.clearFocus();
    }
}
