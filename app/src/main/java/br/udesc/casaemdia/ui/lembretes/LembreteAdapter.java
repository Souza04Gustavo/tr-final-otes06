package br.udesc.casaemdia.ui.lembretes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.databinding.ItemLembreteBinding;
import br.udesc.casaemdia.ui.Rotulos;
import br.udesc.casaemdia.util.Formatadores;

/**
 * Lista de lembretes usada na aba Lembretes e no Painel (próximos 7 dias).
 */
public class LembreteAdapter extends ListAdapter<LembreteDto, LembreteAdapter.ViewHolder> {

    /** Ações disparadas por um item da lista. */
    public interface Acoes {
        void abrir(LembreteDto lembrete);

        void concluir(LembreteDto lembrete);
    }

    private static final DiffUtil.ItemCallback<LembreteDto> DIFERENCA = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull LembreteDto antigo, @NonNull LembreteDto novo) {
            return Objects.equals(antigo.getId(), novo.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull LembreteDto antigo, @NonNull LembreteDto novo) {
            return antigo.equals(novo);
        }
    };

    private static final String SEPARADOR = " · ";

    private final boolean modoPainel;
    private final Acoes acoes;

    /**
     * @param modoPainel no painel não há botão de concluir e a data mostra o dia da semana
     */
    public LembreteAdapter(boolean modoPainel, Acoes acoes) {
        super(DIFERENCA);
        this.modoPainel = modoPainel;
        this.acoes = acoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        ItemLembreteBinding binding = ItemLembreteBinding.inflate(LayoutInflater.from(pai.getContext()), pai, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int posicao) {
        holder.exibir(getItem(posicao));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemLembreteBinding binding;

        ViewHolder(ItemLembreteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void exibir(LembreteDto lembrete) {
            Context context = binding.getRoot().getContext();
            LocalDate hoje = LocalDate.now();
            boolean ehHoje = !lembrete.isConcluido() && hoje.equals(lembrete.getDataVencimento());

            // Categoria: filete colorido na borda + nome escrito (a cor nunca é a única pista)
            EstiloCategoria estilo = EstiloCategoria.de(lembrete.getCategoria());
            binding.filete.setBackgroundColor(ContextCompat.getColor(context, estilo.cor));
            binding.categoria.setText(estilo.rotulo);

            binding.titulo.setText(lembrete.getTitulo());
            binding.detalhe.setText(detalhe(context, lembrete));

            // Status em texto, sem selo: "Atrasado há 3 dias" / "Vence hoje"
            if (lembrete.isAtrasado()) {
                int dias = (int) Math.max(1, ChronoUnit.DAYS.between(lembrete.getDataVencimento(), hoje));
                mostrarStatus(context.getResources().getQuantityString(R.plurals.status_atrasado, dias, dias),
                        ContextCompat.getColor(context, R.color.erro));
            } else if (ehHoje) {
                mostrarStatus(context.getString(R.string.status_vence_hoje),
                        ContextCompat.getColor(context, R.color.primaria));
            } else {
                binding.status.setVisibility(View.GONE);
            }

            binding.valor.setVisibility(lembrete.getValor() == null ? View.GONE : View.VISIBLE);
            binding.valor.setText(Formatadores.moeda(lembrete.getValor()));

            boolean podeConcluir = !modoPainel && !lembrete.isConcluido();
            binding.botaoConcluir.setVisibility(podeConcluir ? View.VISIBLE : View.GONE);
            // Sem a caixa de concluir (painel, concluídos), o valor ganha a margem da borda do cartão
            ViewGroup.MarginLayoutParams margens = (ViewGroup.MarginLayoutParams) binding.valor.getLayoutParams();
            margens.setMarginEnd(context.getResources().getDimensionPixelSize(
                    podeConcluir ? R.dimen.margem_valor_com_acao : R.dimen.margem_valor_sem_acao));
            binding.valor.setLayoutParams(margens);
            binding.botaoConcluir.setContentDescription(
                    context.getString(R.string.acao_concluir_lembrete, lembrete.getTitulo()));
            binding.botaoConcluir.setOnClickListener(v -> acoes.concluir(lembrete));

            binding.cartao.setStrokeColor(ContextCompat.getColor(context,
                    lembrete.isAtrasado() ? R.color.erro_contorno : R.color.contorno_suave));
            binding.cartao.setOnClickListener(v -> acoes.abrir(lembrete));
        }

        /** " · 25/11 · Mensal" (vem logo depois do nome da categoria). */
        private String detalhe(Context context, LembreteDto lembrete) {
            if (lembrete.isConcluido() && lembrete.getDataConclusao() != null) {
                return SEPARADOR + context.getString(R.string.concluido_em,
                        Formatadores.diaMes(lembrete.getDataConclusao()));
            }
            String recorrencia = Rotulos.recorrenciaCurta(context, lembrete.getRecorrencia(), lembrete.getIntervaloDias());
            String data = modoPainel
                    ? Formatadores.diaSemanaEData(lembrete.getDataVencimento())
                    : Formatadores.diaMes(lembrete.getDataVencimento());
            return SEPARADOR + data + SEPARADOR + recorrencia;
        }

        private void mostrarStatus(String texto, int cor) {
            binding.status.setVisibility(View.VISIBLE);
            binding.status.setText(texto);
            binding.status.setTextColor(cor);
        }
    }
}
