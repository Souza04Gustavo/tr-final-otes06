package br.udesc.casaemdia.ui.compras;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.ItemCompraDto;
import br.udesc.casaemdia.databinding.ItemCompraBinding;

/**
 * Itens da lista de compras. Tocar marca/desmarca; tocar e segurar abre as opções.
 */
public class ItemCompraAdapter extends ListAdapter<ItemCompraDto, ItemCompraAdapter.ViewHolder> {

    public interface Acoes {
        void alternar(ItemCompraDto item);

        void abrirOpcoes(ItemCompraDto item);
    }

    private static final DiffUtil.ItemCallback<ItemCompraDto> DIFERENCA = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ItemCompraDto antigo, @NonNull ItemCompraDto novo) {
            return Objects.equals(antigo.getId(), novo.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ItemCompraDto antigo, @NonNull ItemCompraDto novo) {
            return antigo.equals(novo);
        }
    };

    private final Acoes acoes;

    public ItemCompraAdapter(Acoes acoes) {
        super(DIFERENCA);
        this.acoes = acoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new ViewHolder(ItemCompraBinding.inflate(LayoutInflater.from(pai.getContext()), pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int posicao) {
        holder.exibir(getItem(posicao));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemCompraBinding binding;

        ViewHolder(ItemCompraBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void exibir(ItemCompraDto item) {
            Context context = binding.getRoot().getContext();
            boolean comprado = item.isComprado();

            binding.marcador.setChecked(comprado);
            binding.nome.setText(item.getNome());
            binding.quantidade.setText(context.getString(R.string.quantidade_formato, item.getQuantidade()));

            // Itens comprados ficam riscados e em cinza
            int flags = binding.nome.getPaintFlags();
            binding.nome.setPaintFlags(comprado ? flags | Paint.STRIKE_THRU_TEXT_FLAG : flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            binding.nome.setTextColor(ContextCompat.getColor(context, comprado ? R.color.texto_secundario : R.color.texto));
            binding.quantidade.setBackgroundResource(comprado ? 0 : R.drawable.bg_quantidade);
            binding.quantidade.setTextColor(ContextCompat.getColor(context,
                    comprado ? R.color.texto_secundario : R.color.texto_rotulo));

            binding.getRoot().setOnClickListener(v -> acoes.alternar(item));
            binding.getRoot().setOnLongClickListener(v -> {
                acoes.abrirOpcoes(item);
                return true;
            });
        }
    }
}
