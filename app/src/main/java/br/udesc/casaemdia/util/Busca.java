package br.udesc.casaemdia.util;

import androidx.annotation.Nullable;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Comparação de textos para a busca: ignora maiúsculas, acentos e espaços extras
 * ("agua" encontra "Água").
 */
public final class Busca {

    private static final Pattern ACENTOS = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACOS = Pattern.compile("\\s+");

    private Busca() {
    }

    /** Texto sem acentos, em minúsculas e com espaços simples. */
    public static String normalizar(@Nullable String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = ACENTOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        return ESPACOS.matcher(semAcentos.toLowerCase(Formatadores.PT_BR)).replaceAll(" ").trim();
    }

    /**
     * @param termo  o que o usuário digitou; vazio corresponde a tudo
     * @param textos os campos onde procurar (nulos são ignorados)
     */
    public static boolean corresponde(@Nullable String termo, @Nullable String... textos) {
        String procurado = normalizar(termo);
        if (procurado.isEmpty()) {
            return true;
        }
        if (textos == null) {
            return false;
        }
        for (String texto : textos) {
            if (normalizar(texto).contains(procurado)) {
                return true;
            }
        }
        return false;
    }
}
