package br.com.zefi.service;

import java.text.Normalizer;
import java.util.List;

/**
 * As 5 categorias de interesse/missao do Soul Up.
 * ATENCAO: devem ter exatamente os mesmos textos usados no banco (CHECK da tabela).
 */
public final class Categorias {

    public static final List<String> VALIDAS =
            List.of("energia", "moda", "alimentação", "reciclagem", "transporte");

    private Categorias() {}

    /** Aceita variacoes de maiuscula/acento e devolve o texto oficial, ou null se invalida. */
    public static String normalizar(String categoria) {
        if (categoria == null) return null;
        String semAcento = removerAcentos(categoria.trim().toLowerCase());
        for (String valida : VALIDAS) {
            if (removerAcentos(valida).equals(semAcento)) return valida;
        }
        return null;
    }

    private static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
