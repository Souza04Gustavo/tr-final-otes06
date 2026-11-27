package br.udesc.casaemdia.ui.painel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.udesc.casaemdia.data.model.PainelDto;
import br.udesc.casaemdia.data.repository.PainelRepository;
import br.udesc.casaemdia.data.repository.ResultadoCallback;
import br.udesc.casaemdia.util.Resource;

public class PainelViewModel extends ViewModel {

    private final PainelRepository repository = new PainelRepository();
    private final MutableLiveData<Resource<PainelDto>> painel = new MutableLiveData<>();

    public LiveData<Resource<PainelDto>> getPainel() {
        return painel;
    }

    public void carregar() {
        painel.setValue(Resource.carregando());
        repository.carregar(new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable PainelDto dados) {
                painel.setValue(Resource.sucesso(dados));
            }

            @Override
            public void erro(String mensagem) {
                painel.setValue(Resource.erro(mensagem));
            }
        });
    }
}
