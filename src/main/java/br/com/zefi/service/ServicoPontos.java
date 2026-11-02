package br.com.zefi.service;

import br.com.zefi.model.Missao;
import br.com.zefi.model.Recompensa;
import br.com.zefi.model.Usuario;

import java.util.List;
import java.util.Optional;

public final class ServicoPontos {

    private static final double BONUS_PERSONALIZACAO = 0.10; // 10% de bonus por missao personalizada

    // Catalogo de recompensas reais do Soul Up, com o custo em pontos
    private static final List<Recompensa> CATALOGO = List.of(
            new Recompensa(1, "Cashback via Pix", 50),
            new Recompensa(2, "Desconto na conta de luz", 100),
            new Recompensa(3, "Compensação de carbono", 150)
    );

    private ServicoPontos() {}

    public static boolean combinaComInteresse(Missao missao, Usuario usuario) {
        return missao.getCategoria() != null
                && missao.getCategoria().equalsIgnoreCase(usuario.getCategoriaInteresse());
    }

    public static int calcularPontosPorMissao(Missao missao, Usuario usuario) {
        int pontosBase = missao.getPontosRecompensa();
        if (combinaComInteresse(missao, usuario)) {
            return pontosBase + (int) Math.round(pontosBase * BONUS_PERSONALIZACAO);
        }
        return pontosBase;
    }

    public static int atualizarSaldo(Usuario usuario, int pontosGanhos) {
        return usuario.getPontosAcumulados() + pontosGanhos;
    }

    public static boolean verificarElegibilidadeRecompensa(Usuario usuario, int pontosNecessarios) {
        return usuario.getPontosAcumulados() >= pontosNecessarios;
    }

    public static List<Recompensa> getCatalogo() {
        return CATALOGO;
    }

    public static Optional<Recompensa> buscarRecompensa(int id) {
        return CATALOGO.stream().filter(r -> r.id() == id).findFirst();
    }

    // Quanto falta para a recompensa mais barata que o usuario ainda nao pode resgatar
    public static int pontosFaltantesProximaRecompensa(Usuario usuario) {
        int menorFaltante = Integer.MAX_VALUE;
        for (Recompensa r : CATALOGO) {
            if (r.custo() > usuario.getPontosAcumulados()) {
                menorFaltante = Math.min(menorFaltante, r.custo() - usuario.getPontosAcumulados());
            }
        }
        return menorFaltante == Integer.MAX_VALUE ? 0 : menorFaltante;
    }
}
