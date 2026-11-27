package br.udesc.casaemdia.ui.compras;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.ItemCompraDto;
import br.udesc.casaemdia.data.repository.CompraRepository;
import br.udesc.casaemdia.data.repository.ResultadoCallback;
import br.udesc.casaemdia.util.Aviso;
import br.udesc.casaemdia.util.Evento;
import br.udesc.casaemdia.util.Resource;

/**
 * Lista de compras: após cada alteração, a lista é recarregada da API.
 */
public class ComprasViewModel extends AndroidViewModel {

    private final CompraRepository repository = new CompraRepository();
    private final MutableLiveData<Resource<List<ItemCompraDto>>> itens = new MutableLiveData<>();
    private final MutableLiveData<Evento<Aviso>> avisos = new MutableLiveData<>();
    /** Avisa a tela para limpar o campo depois de adicionar um item. */
    private final MutableLiveData<Evento<Boolean>> itemAdicionado = new MutableLiveData<>();

    public ComprasViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Resource<List<ItemCompraDto>>> getItens() {
        return itens;
    }

    public LiveData<Evento<Aviso>> getAvisos() {
        return avisos;
    }

    public LiveData<Evento<Boolean>> getItemAdicionado() {
        return itemAdicionado;
    }

    public void carregar() {
        itens.setValue(Resource.carregando());
        repository.listar(new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable List<ItemCompraDto> dados) {
                itens.setValue(Resource.sucesso(dados));
            }

            @Override
            public void erro(String mensagem) {
                itens.setValue(Resource.erro(mensagem));
            }
        });
    }

    public void adicionar(String nome, int quantidade) {
        repository.adicionar(nome, quantidade, new RecarregarAoConcluir<>(0) {
            @Override
            public void sucesso(@Nullable ItemCompraDto dados) {
                itemAdicionado.setValue(new Evento<>(true));
                super.sucesso(dados);
            }
        });
    }

    public void alternar(ItemCompraDto item) {
        repository.alternar(item.getId(), new RecarregarAoConcluir<>(0));
    }

    public void editar(ItemCompraDto item, String nome, int quantidade) {
        repository.editar(item.getId(), nome, quantidade, new RecarregarAoConcluir<>(0));
    }

    public void excluir(ItemCompraDto item) {
        repository.excluir(item.getId(), new RecarregarAoConcluir<>(0) {
            @Override
            public void sucesso(@Nullable Void dados) {
                String texto = getApplication().getString(R.string.item_excluido);
                avisos.setValue(new Evento<>(Aviso.comDesfazer(texto, () -> restaurar(item))));
                carregar();
            }
        });
    }

    /** "Desfazer" da exclusão: recria o item com o mesmo nome, quantidade e situação. */
    private void restaurar(ItemCompraDto item) {
        repository.adicionar(item.getNome(), item.getQuantidade(), new RecarregarAoConcluir<>(R.string.item_restaurado) {
            @Override
            public void sucesso(@Nullable ItemCompraDto recriado) {
                if (item.isComprado() && recriado != null) {
                    repository.alternar(recriado.getId(), new RecarregarAoConcluir<>(R.string.item_restaurado));
                } else {
                    super.sucesso(recriado);
                }
            }
        });
    }

    public void limparComprados() {
        repository.limparComprados(new RecarregarAoConcluir<>(R.string.comprados_removidos));
    }

    /** Recarrega a lista após uma alteração e, se houver, mostra uma mensagem de confirmação. */
    private class RecarregarAoConcluir<T> implements ResultadoCallback<T> {

        @StringRes
        private final int mensagemSucesso;

        RecarregarAoConcluir(@StringRes int mensagemSucesso) {
            this.mensagemSucesso = mensagemSucesso;
        }

        @Override
        public void sucesso(@Nullable T dados) {
            if (mensagemSucesso != 0) {
                avisos.setValue(new Evento<>(Aviso.simples(getApplication().getString(mensagemSucesso))));
            }
            carregar();
        }

        @Override
        public void erro(String mensagem) {
            avisos.setValue(new Evento<>(Aviso.simples(mensagem)));
        }
    }
}
