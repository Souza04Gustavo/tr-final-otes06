package br.udesc.casaemdia.ui.painel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.data.model.PainelDto;
import br.udesc.casaemdia.data.model.ResumoMesDto;
import br.udesc.casaemdia.data.session.SessionManager;
import br.udesc.casaemdia.databinding.FragmentPainelBinding;
import br.udesc.casaemdia.ui.Avisos;
import br.udesc.casaemdia.ui.lembretes.LembreteAdapter;
import br.udesc.casaemdia.ui.lembretes.LembreteFormActivity;
import br.udesc.casaemdia.ui.main.TelaPrincipal;
import br.udesc.casaemdia.util.Formatadores;
import br.udesc.casaemdia.util.Resource;

/**
 * T3 – Painel: atrasados, para hoje, contas do mês, itens a comprar e próximos 7 dias.
 */
public class PainelFragment extends Fragment {

    private FragmentPainelBinding binding;
    private PainelViewModel viewModel;
    private LembreteAdapter adapterProximos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentPainelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapterProximos = new LembreteAdapter(true, new LembreteAdapter.Acoes() {
            @Override
            public void abrir(LembreteDto lembrete) {
                startActivity(LembreteFormActivity.intentEditar(requireContext(), lembrete.getId()));
            }

            @Override
            public void concluir(LembreteDto lembrete) {
                // No painel não há botão de concluir
            }
        });
        binding.listaProximos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaProximos.setAdapter(adapterProximos);

        binding.cartaoAtrasados.setOnClickListener(v -> telaPrincipal().irParaLembretes());
        binding.cartaoHoje.setOnClickListener(v -> telaPrincipal().irParaLembretes());
        binding.cartaoCompras.setOnClickListener(v -> telaPrincipal().irParaCompras());
        binding.cartaoContas.setOnClickListener(v -> telaPrincipal().irParaGastos());

        viewModel = new ViewModelProvider(this).get(PainelViewModel.class);
        viewModel.getPainel().observe(getViewLifecycleOwner(), this::exibir);
        binding.atualizar.setOnRefreshListener(viewModel::carregar);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarrega sempre que a aba aparece (ex.: ao voltar do formulário)
        definirCabecalho(SessionManager.getInstance(requireContext()).getNomeCasa());
        viewModel.carregar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void exibir(Resource<PainelDto> estado) {
        binding.atualizar.setRefreshing(estado.isCarregando());
        if (estado.getStatus() == Resource.Status.ERRO) {
            Avisos.criar(binding.getRoot(), String.valueOf(estado.getMensagem()))
                    .setAction(R.string.acao_tentar_novamente, v -> viewModel.carregar())
                    .show();
            return;
        }
        PainelDto painel = estado.getDados();
        if (estado.getStatus() != Resource.Status.SUCESSO || painel == null) {
            return;
        }

        definirCabecalho(painel.getNomeCasa());
        binding.quantidadeAtrasados.setText(String.valueOf(painel.getAtrasados().size()));
        binding.quantidadeHoje.setText(String.valueOf(painel.getHoje().size()));
        exibirResumo(painel.getResumoMes());

        int itens = (int) painel.getItensPendentesCompra();
        binding.textoCompras.setText(getResources().getQuantityString(R.plurals.painel_itens_compra, itens, itens));

        adapterProximos.submitList(painel.getProximos7Dias());
        binding.proximosVazio.setVisibility(painel.getProximos7Dias().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void exibirResumo(@Nullable ResumoMesDto resumo) {
        if (resumo == null) {
            return;
        }
        binding.tituloContas.setText(getString(R.string.painel_contas_mes, Formatadores.nomeDoMes(resumo.getMes())));
        binding.totalContas.setText(Formatadores.moeda(resumo.getTotalContas()));
        binding.totalPago.setText(Formatadores.moeda(resumo.getTotalPago()));
        binding.totalPendente.setText(Formatadores.moeda(resumo.getTotalPendente()));

        int percentual = percentualPago(resumo);
        binding.progressoContas.setProgressCompat(percentual, true);
        binding.progressoContas.setContentDescription(getString(R.string.painel_progresso_descricao, percentual));
    }

    private static int percentualPago(ResumoMesDto resumo) {
        BigDecimal total = resumo.getTotalContas();
        if (total == null || total.signum() == 0 || resumo.getTotalPago() == null) {
            return 0;
        }
        return resumo.getTotalPago().multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private void definirCabecalho(String nomeCasa) {
        telaPrincipal().definirCabecalho(nomeCasa, Formatadores.dataPorExtenso(LocalDate.now()));
    }

    private TelaPrincipal telaPrincipal() {
        return (TelaPrincipal) requireActivity();
    }
}
