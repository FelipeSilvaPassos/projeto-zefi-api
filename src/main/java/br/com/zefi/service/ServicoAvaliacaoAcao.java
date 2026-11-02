package br.com.zefi.service;

import br.com.zefi.model.Usuario;

import java.util.LinkedHashMap;
import java.util.Map;

/** O Zefi le a acao descrita pelo usuario e identifica a categoria por palavras-chave. */
public final class ServicoAvaliacaoAcao {

    private static final int PONTOS_BASE = 10;
    private static final int PONTOS_POR_PALAVRA_CHAVE = 5;
    private static final int PONTOS_MAXIMOS = 50;
    private static final double BONUS_INTERESSE = 0.10;

    // Categoria -> palavras-chave que o Zefi reconhece no texto
    private static final Map<String, String[]> PALAVRAS_CHAVE = new LinkedHashMap<>();
    static {
        PALAVRAS_CHAVE.put("energia", new String[]{"luz", "energia", "solar", "lâmpada", "eletricidade", "led"});
        PALAVRAS_CHAVE.put("moda", new String[]{"roupa", "brechó", "sustentável", "moda", "reciclada", "costura"});
        PALAVRAS_CHAVE.put("alimentação", new String[]{"comida", "horta", "orgânico", "compostagem", "verdura", "plantei"});
        PALAVRAS_CHAVE.put("reciclagem", new String[]{"reciclagem", "reciclar", "lixo", "separei", "coleta seletiva", "descarte"});
        PALAVRAS_CHAVE.put("transporte", new String[]{"bicicleta", "caminhada", "carona", "ônibus", "metrô", "pedalei", "bike"});
    }

    private ServicoAvaliacaoAcao() {}

    public record ResultadoAvaliacao(String categoria, int pontos, String mensagemZefi) {}

    public static ResultadoAvaliacao avaliar(String descricao, Usuario usuario) {
        String textoLower = descricao.toLowerCase();

        String melhorCategoria = null;
        int maisPalavrasEncontradas = 0;

        // Verifica qual categoria teve mais palavras-chave no texto
        for (Map.Entry<String, String[]> entry : PALAVRAS_CHAVE.entrySet()) {
            int encontradas = 0;
            for (String palavra : entry.getValue()) {
                if (textoLower.contains(palavra)) encontradas++;
            }
            if (encontradas > maisPalavrasEncontradas) {
                maisPalavrasEncontradas = encontradas;
                melhorCategoria = entry.getKey();
            }
        }

        // Sem palavra-chave reconhecida: categoria "geral" com pontuacao minima
        if (melhorCategoria == null) melhorCategoria = "geral";

        int pontos = PONTOS_BASE + (maisPalavrasEncontradas * PONTOS_POR_PALAVRA_CHAVE);

        boolean combinaComInteresse = melhorCategoria.equalsIgnoreCase(usuario.getCategoriaInteresse());
        if (combinaComInteresse) pontos += (int) Math.round(pontos * BONUS_INTERESSE);

        pontos = Math.min(pontos, PONTOS_MAXIMOS);

        return new ResultadoAvaliacao(melhorCategoria, pontos,
                gerarMensagem(melhorCategoria, pontos, combinaComInteresse, usuario));
    }

    private static String gerarMensagem(String categoria, int pontos, boolean combina, Usuario usuario) {
        if (categoria.equals("geral")) {
            return "Legal, " + usuario.getNome() + "! Registrei sua ação. Toda atitude conta, mesmo que pequena! Você ganhou " + pontos + " pontos.";
        }
        if (combina) {
            return "Incrível, " + usuario.getNome() + "! Vi que sua ação foi de " + categoria
                    + ", exatamente o seu interesse! Você ganhou " + pontos + " pontos (com bônus de personalização)! 🌱";
        }
        return "Muito bom! Identifiquei sua ação como categoria \"" + categoria + "\". Você ganhou " + pontos + " pontos!";
    }
}
