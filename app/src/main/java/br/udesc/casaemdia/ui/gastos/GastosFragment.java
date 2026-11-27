package br.udesc.casaemdia.ui.gastos;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.AnaliseGastosDto;
import br.udesc.casaemdia.databinding.FragmentGastosBinding;
import br.udesc.casaemdia.databinding.ItemGastoCategoriaBinding;
import br.udesc.casaemdia.databinding.ItemMaiorGastoBinding;
import br.udesc.casaemdia.ui.Avisos;
import br.udesc.casaemdia.ui.lembretes.EstiloCategoria;
import br.udesc.casaemdia.ui.main.TelaPrincipal;
import br.udesc.casaemdia.util.Formatadores;
import br.udesc.casaemdia.util.Resource;

/**
 * T7 – Análise de gastos: resumo do mês, distribuição por categoria, maiores gastos e histórico.
 * <p>
 * Formas escolhidas pela tarefa de cada informação: números em destaque para o resumo, barra segmentada
 * fina + lista para a divisão por categoria, barras horizontais ordenadas (uma só cor) para comparar os
 * maiores gastos e colunas com o mês em destaque para o histórico.
 */
public class GastosFragment extends Fragment {

    private static final float OPACIDADE_CARREGANDO = 0.6f;

    private FragmentGastosBinding binding;
    private GastosViewModel viewModel;
    /** Histórico exibido no gráfico, para traduzir o toque em uma coluna no mês correspondente. */
    private List<AnaliseGastosDto.GastoMensal> historicoExibido = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentGastosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GastosViewModel.class);
        viewModel.getMes().observe(getViewLifecycleOwner(),
                mes -> binding.tituloMes.setText(Formatadores.mesAno(mes)));
        viewModel.getAnalise().observe(getViewLifecycleOwner(), this::exibir);

        binding.botaoMesAnterior.setOnClickListener(v -> viewModel.mesAnterior());
        binding.botaoMesSeguinte.setOnClickListener(v -> viewModel.proximoMes());
        binding.atualizar.setOnRefreshListener(viewModel::carregar);
        binding.graficoMensal.setAoSelecionarMes(indice -> {
            if (indice < historicoExibido.size()) {
                viewModel.irPara(YearMonth.parse(historicoExibido.get(indice).getMes()));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TelaPrincipal) requireActivity()).definirCabecalho(getString(R.string.gastos_titulo), null);
        viewModel.carregar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void exibir(Resource<AnaliseGastosDto> estado) {
        binding.atualizar.setRefreshing(estado.isCarregando());
        // Ao trocar de mês, o conteúdo anterior fica esmaecido em vez de sumir (sem "pulo" na tela)
        binding.conteudo.setAlpha(estado.isCarregando() ? OPACIDADE_CARREGANDO : 1f);
        if (estado.getStatus() == Resource.Status.ERRO) {
            Avisos.criar(binding.getRoot(), String.valueOf(estado.getMensagem()))
                    .setAction(R.string.acao_tentar_novamente, v -> viewModel.carregar())
                    .show();
            return;
        }
        AnaliseGastosDto analise = estado.getDados();
        if (estado.getStatus() != Resource.Status.SUCESSO || analise == null) {
            return;
        }

        exibirResumo(analise);

        boolean semGastos = analise.getTotal() == null || analise.getTotal().signum() == 0;
        binding.vazio.setVisibility(semGastos ? View.VISIBLE : View.GONE);
        binding.vazio.setText(getString(R.string.gastos_vazio, Formatadores.nomeDoMes(analise.getMes())));
        binding.cartaoCategorias.setVisibility(semGastos ? View.GONE : View.VISIBLE);
        binding.cartaoMaiores.setVisibility(semGastos ? View.GONE : View.VISIBLE);
        if (!semGastos) {
            exibirCategorias(analise.getPorCategoria());
            exibirMaioresGastos(analise.getMaioresGastos());
        }
        exibirHistorico(analise);
    }

    private void exibirResumo(AnaliseGastosDto analise) {
        BigDecimal total = valor(analise.getTotal());
        BigDecimal pago = valor(analise.getPago());
        binding.total.setText(Formatadores.moeda(total));
        binding.pago.setText(Formatadores.moeda(pago));
        binding.pendente.setText(Formatadores.moeda(analise.getPendente()));

        int percentualPago = total.signum() == 0 ? 0
                : pago.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.HALF_UP).intValue();
        binding.progressoPago.setProgressCompat(percentualPago, true);
        binding.progressoPago.setContentDescription(getString(R.string.gastos_progresso_descricao, percentualPago));

        exibirVariacao(analise);

        int lancamentos = analise.getQuantidadeLancamentos();
        binding.media.setText(getString(R.string.gastos_media_e_lancamentos,
                Formatadores.moeda(analise.getMediaMensal()),
                getResources().getQuantityString(R.plurals.gastos_lancamentos, lancamentos, lancamentos)));
    }

    /** Gastar mais que no mês anterior é sinalizado em vermelho com ▲; gastar menos, com a cor de sucesso e ▼. */
    private void exibirVariacao(AnaliseGastosDto analise) {
        String mesAnterior = Formatadores.nomeDoMes(YearMonth.parse(analise.getMes()).minusMonths(1).toString());
        BigDecimal variacao = analise.getVariacaoPercentual();
        int cor;
        String texto;
        if (variacao == null) {
            texto = getString(R.string.gastos_sem_comparacao, mesAnterior);
            cor = R.color.texto_secundario;
        } else if (variacao.signum() > 0) {
            texto = getString(R.string.gastos_variacao_alta, Formatadores.percentual(variacao), mesAnterior);
            cor = R.color.erro;
        } else if (variacao.signum() < 0) {
            texto = getString(R.string.gastos_variacao_queda, Formatadores.percentual(variacao), mesAnterior);
            cor = R.color.sucesso;
        } else {
            texto = getString(R.string.gastos_variacao_igual, mesAnterior);
            cor = R.color.texto_secundario;
        }
        binding.variacao.setText(texto);
        binding.variacao.setTextColor(ContextCompat.getColor(requireContext(), cor));
    }

    private void exibirCategorias(List<AnaliseGastosDto.GastoCategoria> categorias) {
        Context context = requireContext();
        List<BarraSegmentadaView.Segmento> segmentos = new ArrayList<>();
        binding.listaCategorias.removeAllViews();

        for (AnaliseGastosDto.GastoCategoria fatia : categorias) {
            EstiloCategoria estilo = EstiloCategoria.de(fatia.getCategoria());
            int cor = ContextCompat.getColor(context, estilo.cor);
            segmentos.add(new BarraSegmentadaView.Segmento(cor, valor(fatia.getValor()).floatValue()));

            ItemGastoCategoriaBinding linha =
                    ItemGastoCategoriaBinding.inflate(getLayoutInflater(), binding.listaCategorias, false);
            String nome = getString(estilo.nome);
            String valor = Formatadores.moeda(fatia.getValor());
            linha.cor.setBackgroundTintList(ColorStateList.valueOf(cor));
            linha.nome.setText(nome);
            linha.valor.setText(valor);
            linha.percentual.setText(getString(R.string.gastos_percentual, fatia.getPercentual()));
            descreverLinha(linha.getRoot(), nome, valor, fatia.getPercentual());
            binding.listaCategorias.addView(linha.getRoot());
        }
        binding.barraCategorias.setSegmentos(segmentos);
    }

    /** Barras proporcionais ao maior valor da lista; a linha "Outros" fica em cinza por ser um agrupamento. */
    private void exibirMaioresGastos(List<AnaliseGastosDto.MaiorGasto> maiores) {
        Context context = requireContext();
        binding.listaMaiores.removeAllViews();

        float maximo = 0f;
        for (AnaliseGastosDto.MaiorGasto gasto : maiores) {
            maximo = Math.max(maximo, valor(gasto.getValor()).floatValue());
        }

        for (AnaliseGastosDto.MaiorGasto gasto : maiores) {
            ItemMaiorGastoBinding linha =
                    ItemMaiorGastoBinding.inflate(getLayoutInflater(), binding.listaMaiores, false);
            String valor = Formatadores.moeda(gasto.getValor());
            linha.titulo.setText(gasto.getTitulo());
            linha.valor.setText(valor);
            linha.percentual.setText(getString(R.string.gastos_percentual, gasto.getPercentual()));

            int proporcao = maximo <= 0f ? 0 : Math.round(valor(gasto.getValor()).floatValue() / maximo * 100f);
            linha.barra.setProgressCompat(Math.max(proporcao, 2), false);
            if (gasto.isAgrupado()) {
                linha.titulo.setTextColor(ContextCompat.getColor(context, R.color.texto_secundario));
                linha.barra.setIndicatorColor(ContextCompat.getColor(context, R.color.grafico_contexto));
            }
            descreverLinha(linha.getRoot(), gasto.getTitulo(), valor, gasto.getPercentual());
            binding.listaMaiores.addView(linha.getRoot());
        }
    }

    private void exibirHistorico(AnaliseGastosDto analise) {
        historicoExibido = new ArrayList<>(analise.getHistorico());
        int meses = historicoExibido.size();
        binding.tituloHistorico.setText(getResources().getQuantityString(R.plurals.gastos_historico, meses, meses));

        List<GraficoMensalView.Coluna> colunas = new ArrayList<>();
        int selecionada = -1;
        for (int i = 0; i < historicoExibido.size(); i++) {
            AnaliseGastosDto.GastoMensal mes = historicoExibido.get(i);
            YearMonth anoMes = YearMonth.parse(mes.getMes());
            String valor = Formatadores.moeda(mes.getTotal());
            colunas.add(new GraficoMensalView.Coluna(
                    Formatadores.mesCurto(anoMes),
                    valor(mes.getTotal()).floatValue(),
                    valor,
                    Formatadores.mesAno(anoMes) + ": " + valor));
            if (mes.getMes().equals(analise.getMes())) {
                selecionada = i;
            }
        }
        binding.graficoMensal.setDados(colunas, selecionada);
    }

    /** O leitor de tela lê a linha inteira de uma vez: "Contas: R$ 1.470,80, 87% do mês". */
    private void descreverLinha(View linha, String nome, String valor, int percentual) {
        linha.setContentDescription(getString(R.string.gastos_item_descricao, nome, valor, percentual));
        ViewCompat.setScreenReaderFocusable(linha, true);
    }

    private static BigDecimal valor(@Nullable BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
