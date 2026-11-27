package br.udesc.casaemdia.data.model;

import java.util.List;

/** Formato padrão de erro devolvido pelo back-end. */
public class ErroResponse {

    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<CampoErro> campos;

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public List<CampoErro> getCampos() {
        return campos;
    }

    /** Erro de validação de um campo. */
    public static class CampoErro {

        private String campo;
        private String mensagem;

        public String getCampo() {
            return campo;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}
