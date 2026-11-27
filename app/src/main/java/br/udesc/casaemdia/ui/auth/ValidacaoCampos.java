package br.udesc.casaemdia.ui.auth;

import android.util.Patterns;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import br.udesc.casaemdia.R;

/**
 * Validações simples feitas no app antes de chamar a API (a API valida de novo).
 */
final class ValidacaoCampos {

    static final int TAMANHO_MINIMO_SENHA = 6;

    private ValidacaoCampos() {
    }

    static String texto(TextInputLayout campo) {
        return campo.getEditText() == null || campo.getEditText().getText() == null
                ? "" : campo.getEditText().getText().toString().trim();
    }

    /** @return {@code true} se preenchido; caso contrário, exibe o erro no campo */
    static boolean obrigatorio(TextInputLayout campo) {
        boolean ok = !texto(campo).isEmpty();
        definirErro(campo, ok ? null : campo.getContext().getString(R.string.erro_campo_obrigatorio));
        return ok;
    }

    static boolean email(TextInputLayout campo) {
        boolean ok = Patterns.EMAIL_ADDRESS.matcher(texto(campo)).matches();
        definirErro(campo, ok ? null : campo.getContext().getString(R.string.erro_email_invalido));
        return ok;
    }

    static boolean senha(TextInputLayout campo) {
        String senha = campo.getEditText() == null ? "" : String.valueOf(campo.getEditText().getText());
        boolean ok = senha.length() >= TAMANHO_MINIMO_SENHA;
        definirErro(campo, ok ? null : campo.getContext().getString(R.string.erro_senha_curta));
        return ok;
    }

    private static void definirErro(TextInputLayout campo, @Nullable String erro) {
        campo.setError(erro);
        campo.setErrorEnabled(erro != null);
    }
}
