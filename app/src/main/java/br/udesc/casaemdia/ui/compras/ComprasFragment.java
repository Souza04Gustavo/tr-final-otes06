package br.udesc.casaemdia.ui.compras;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.ItemCompraDto;
import br.udesc.casaemdia.databinding.DialogEditarItemBinding;
import br.udesc.casaemdia.databinding.FragmentComprasBinding;
import br.udesc.casaemdia.ui.Avisos;
import br.udesc.casaemdia.ui.main.TelaPrincipal;
import br.udesc.casaemdia.util.Resource;

/**
 * T6 – Lista de compras.
 */
public class ComprasFragment extends Fragment implements ItemCompraAdapter.Acoes {

    private static final int QUANTIDADE_PADRAO = 1;

    private FragmentComprasBinding binding;
    private ComprasViewModel viewModel;
    private ItemCompraAdapter adapterPendentes;
    private ItemCompraAdapter adapterComprados;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentComprasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapterPendentes = new ItemCompraAdapter(this);
        adapterComprados = new ItemCompraAdapter(this);
        configurarLista(binding.listaPendentes, adapterPendentes);
        configurarLista(binding.listaComprados, adapterComprados);

        viewModel = new ViewModelProvider(this).get(ComprasViewModel.class);
        viewModel.getItens().observe(getViewLifecycleOwner(), this::exibir);
        viewModel.getAvisos().observe(getViewLifecycleOwner(), evento -> Avisos.mostrar(binding.getRoot(), evento));
        viewModel.getItemAdicionado().observe(getViewLifecycleOwner(), evento -> {
            if (evento.consumir() != null) {
                binding.campoItemTexto.setText("");
                binding.campoQuantidadeTexto.setText(String.valueOf(QUANTIDADE_PADRAO));
                binding.campoItemTexto.requestFocus();
            }
        });

        binding.botaoAdicionar.setOnClickListener(v -> adicionar());
        binding.campoQuantidadeTexto.setOnEditorActionListener((v, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_DONE) {
                adicionar();
                return true;
            }
            return false;
        });
        binding.botaoLimpar.setOnClickListener(v -> viewModel.limparComprados());
        binding.atualizar.setOnRefreshListener(viewModel::carregar);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TelaPrincipal) requireActivity()).definirCabecalho(getString(R.string.compras_titulo), null);
        viewModel.carregar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void alternar(ItemCompraDto item) {
        viewModel.alternar(item);
    }

    @Override
    public void abrirOpcoes(ItemCompraDto item) {
        String[] opcoes = {getString(R.string.acao_editar), getString(R.string.acao_excluir)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.getNome())
                .setItems(opcoes, (dialogo, escolha) -> {
                    if (escolha == 0) {
                        editar(item);
                    } else {
                        viewModel.excluir(item);
                    }
                })
                .show();
    }

    private void adicionar() {
        String nome = String.valueOf(binding.campoItemTexto.getText()).trim();
        if (nome.isEmpty()) {
            binding.campoItem.setError(getString(R.string.erro_campo_obrigatorio));
            return;
        }
        binding.campoItem.setError(null);
        viewModel.adicionar(nome, lerQuantidade(String.valueOf(binding.campoQuantidadeTexto.getText())));
    }

    private void editar(ItemCompraDto item) {
        DialogEditarItemBinding dialogo = DialogEditarItemBinding.inflate(getLayoutInflater());
        dialogo.campoNomeTexto.setText(item.getNome());
        dialogo.campoQuantidadeTexto.setText(String.valueOf(item.getQuantidade()));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.editar_item)
                .setView(dialogo.getRoot())
                .setNegativeButton(R.string.acao_cancelar, null)
                .setPositiveButton(R.string.acao_salvar, (d, botao) -> {
                    String nome = String.valueOf(dialogo.campoNomeTexto.getText()).trim();
                    if (!nome.isEmpty()) {
                        viewModel.editar(item, nome, lerQuantidade(String.valueOf(dialogo.campoQuantidadeTexto.getText())));
                    }
                })
                .show();
    }

    private void exibir(Resource<List<ItemCompraDto>> estado) {
        binding.atualizar.setRefreshing(estado.isCarregando());
        if (estado.getStatus() == Resource.Status.ERRO) {
            Avisos.criar(binding.getRoot(), String.valueOf(estado.getMensagem()))
                    .setAction(R.string.acao_tentar_novamente, v -> viewModel.carregar())
                    .show();
            return;
        }
        if (estado.getStatus() != Resource.Status.SUCESSO || estado.getDados() == null) {
            return;
        }

        List<ItemCompraDto> pendentes = new ArrayList<>();
        List<ItemCompraDto> comprados = new ArrayList<>();
        for (ItemCompraDto item : estado.getDados()) {
            (item.isComprado() ? comprados : pendentes).add(item);
        }

        adapterPendentes.submitList(pendentes);
        adapterComprados.submitList(comprados);
        binding.cartaoPendentes.setVisibility(pendentes.isEmpty() ? View.GONE : View.VISIBLE);
        binding.cabecalhoComprados.setVisibility(comprados.isEmpty() ? View.GONE : View.VISIBLE);
        binding.cartaoComprados.setVisibility(comprados.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tituloComprados.setText(getString(R.string.compras_comprados, comprados.size()));

        boolean listaVazia = pendentes.isEmpty() && comprados.isEmpty();
        binding.vazio.setVisibility(listaVazia ? View.VISIBLE : View.GONE);
        binding.dica.setVisibility(listaVazia ? View.GONE : View.VISIBLE);

        String subtitulo = listaVazia ? null
                : pendentes.isEmpty() ? getString(R.string.compras_tudo_comprado)
                : getResources().getQuantityString(R.plurals.compras_subtitulo, pendentes.size(), pendentes.size());
        ((TelaPrincipal) requireActivity()).definirCabecalho(getString(R.string.compras_titulo), subtitulo);
    }

    private void configurarLista(RecyclerView lista, ItemCompraAdapter adapter) {
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);
        MaterialDividerItemDecoration divisor =
                new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);
        divisor.setDividerColorResource(requireContext(), R.color.divisor);
        divisor.setLastItemDecorated(false);
        lista.addItemDecoration(divisor);
    }

    private static int lerQuantidade(String texto) {
        try {
            int quantidade = Integer.parseInt(texto.trim());
            return Math.max(quantidade, QUANTIDADE_PADRAO);
        } catch (NumberFormatException e) {
            return QUANTIDADE_PADRAO;
        }
    }
}
