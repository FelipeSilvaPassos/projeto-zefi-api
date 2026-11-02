package br.com.zefi.service;

import br.com.zefi.model.Missao;
import br.com.zefi.model.Usuario;

import java.util.List;

/** Regras e falas do Zefi para recomendacao de missoes. */
public final class ServicoRecomendacao {

    // Recorrencia minima antes de o Zefi mandar um lembrete (evita incomodar o usuario a toa)
    public static final int IGNORADAS_PARA_LEMBRETE = 3;

    private ServicoRecomendacao() {}

    /** Prioriza a missao de maior pontuacao da categoria de interesse; senao, a maior geral. */
    public static Missao gerarRecomendacao(Usuario usuario, List<Missao> missoesDisponiveis) {
        if (missoesDisponiveis == null || missoesDisponiveis.isEmpty()) return null;

        Missao melhorDaCategoria = null;
        Missao melhorGeral = null;
        for (Missao m : missoesDisponiveis) {
            if (melhorGeral == null || m.getPontosRecompensa() > melhorGeral.getPontosRecompensa()) {
                melhorGeral = m;
            }
            if (ServicoPontos.combinaComInteresse(m, usuario)
                    && (melhorDaCategoria == null || m.getPontosRecompensa() > melhorDaCategoria.getPontosRecompensa())) {
                melhorDaCategoria = m;
            }
        }
        return melhorDaCategoria != null ? melhorDaCategoria : melhorGeral;
    }

    public static String gerarMensagemZefi(Usuario usuario, Missao recomendada) {
        if (recomendada == null) {
            return "Oi, " + usuario.getNome() + "! No momento não tenho nenhuma missão pra te sugerir. Volte mais tarde! 🍃";
        }
        if (ServicoPontos.combinaComInteresse(recomendada, usuario)) {
            return "Oi, " + usuario.getNome() + "! Notei que você curte " + usuario.getCategoriaInteresse()
                    + ". Que tal a missão \"" + recomendada.getNome() + "\"? Ela rende "
                    + ServicoPontos.calcularPontosPorMissao(recomendada, usuario) + " pontos com o seu bônus! 🌱";
        }
        return "Oi, " + usuario.getNome() + "! Essa missão está em alta: \"" + recomendada.getNome()
                + "\". Ela rende " + recomendada.getPontosRecompensa() + " pontos! 🌬️";
    }

    /** Lembrete so aparece quando ha um padrao de recomendacoes ignoradas. */
    public static String gerarLembreteReengajamento(Usuario usuario, int recomendacoesIgnoradas) {
        if (recomendacoesIgnoradas >= IGNORADAS_PARA_LEMBRETE) {
            return usuario.getNome() + ", faz um tempo que você não completa nenhuma missão. "
                    + "Que tal começar com algo rápido hoje? Toda ação conta! 🍃";
        }
        return null;
    }

    public static String gerarMensagemCelebracao(int pontosGanhos, int novoSaldo) {
        return "Boa! Você ganhou " + pontosGanhos + " pontos 🎉 Seu saldo agora é " + novoSaldo + " pontos!";
    }

    public static String gerarResumoProgresso(Usuario usuario) {
        int faltante = ServicoPontos.pontosFaltantesProximaRecompensa(usuario);
        if (faltante == 0) {
            return "Você já pode resgatar qualquer recompensa do catálogo! 🌟";
        }
        return "Faltam apenas " + faltante + " pontos para sua próxima recompensa!";
    }
}
