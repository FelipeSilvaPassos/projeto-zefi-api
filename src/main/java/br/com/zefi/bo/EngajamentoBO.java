package br.com.zefi.bo;

import br.com.zefi.dao.AcaoSustentavelDAO;
import br.com.zefi.dao.RecomendacaoAvatarDAO;
import br.com.zefi.dto.EngajamentoResponse;
import br.com.zefi.model.NivelEngajamento;
import br.com.zefi.model.Usuario;
import br.com.zefi.service.ServicoRecomendacao;
import org.springframework.stereotype.Service;

/**
 * Calcula o nivel de engajamento (BAIXO, MEDIO, ALTO), que muda o visual do Zefi no front.
 * Regra baseada em padrao e recorrencia (ultimos 7 dias), e nao em um unico evento,
 * para evitar falsos positivos. Evolucao prevista: substituir pela previsao do
 * modelo de IA (classificacao de taxa de engajamento) via API Flask.
 */
@Service
public class EngajamentoBO {

    private static final int DIAS_ANALISADOS = 7;
    private static final int ACOES_PARA_ALTO = 3;

    private final AcaoSustentavelDAO acaoDAO;
    private final RecomendacaoAvatarDAO recomendacaoDAO;
    private final UsuarioBO usuarioBO;

    public EngajamentoBO(AcaoSustentavelDAO acaoDAO, RecomendacaoAvatarDAO recomendacaoDAO, UsuarioBO usuarioBO) {
        this.acaoDAO = acaoDAO;
        this.recomendacaoDAO = recomendacaoDAO;
        this.usuarioBO = usuarioBO;
    }

    public EngajamentoResponse calcular(String cpf) {
        Usuario usuario = usuarioBO.buscar(cpf);
        int acoes = acaoDAO.contarDesde(cpf, DIAS_ANALISADOS);
        int ignoradas = recomendacaoDAO.contarRecomendacoesNaoAceitas(cpf);

        NivelEngajamento nivel;
        String mensagem;
        if (acoes >= ACOES_PARA_ALTO) {
            nivel = NivelEngajamento.ALTO;
            mensagem = "Tá ventando forte por aqui, " + usuario.getNome() + "! " + acoes + " ações nesta semana! 🌪️✨";
        } else if (acoes == 0 && ignoradas >= ServicoRecomendacao.IGNORADAS_PARA_LEMBRETE) {
            nivel = NivelEngajamento.BAIXO;
            mensagem = "O vento parou por aqui... senti sua falta! Bora uma missão rapidinha? 🍃";
        } else {
            nivel = NivelEngajamento.MEDIO;
            mensagem = "Bom te ver, " + usuario.getNome() + "! Que tal mais uma ação hoje? 🌬️";
        }
        return new EngajamentoResponse(nivel, acoes, ignoradas, mensagem);
    }
}
