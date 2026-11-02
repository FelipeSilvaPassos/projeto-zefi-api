package br.com.zefi.bo;

import br.com.zefi.dao.MissaoDAO;
import br.com.zefi.dao.RecomendacaoAvatarDAO;
import br.com.zefi.dao.UsuarioDAO;
import br.com.zefi.dto.AceiteResponse;
import br.com.zefi.dto.RecomendacaoResponse;
import br.com.zefi.exception.RecursoNaoEncontradoException;
import br.com.zefi.exception.RegraNegocioException;
import br.com.zefi.model.Missao;
import br.com.zefi.model.RecomendacaoAvatar;
import br.com.zefi.model.Usuario;
import br.com.zefi.service.Auditoria;
import br.com.zefi.service.ServicoPontos;
import br.com.zefi.service.ServicoRecomendacao;
import org.springframework.stereotype.Service;

import java.util.List;

/** O Zefi recomenda missoes e credita os pontos quando o usuario aceita. */
@Service
public class RecomendacaoBO {

    private final RecomendacaoAvatarDAO recomendacaoDAO;
    private final MissaoDAO missaoDAO;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioBO usuarioBO;

    public RecomendacaoBO(RecomendacaoAvatarDAO recomendacaoDAO, MissaoDAO missaoDAO,
                          UsuarioDAO usuarioDAO, UsuarioBO usuarioBO) {
        this.recomendacaoDAO = recomendacaoDAO;
        this.missaoDAO = missaoDAO;
        this.usuarioDAO = usuarioDAO;
        this.usuarioBO = usuarioBO;
    }

    public RecomendacaoResponse recomendar(String cpf) {
        Usuario usuario = usuarioBO.buscar(cpf);
        int ignoradas = recomendacaoDAO.contarRecomendacoesNaoAceitas(cpf);
        String lembrete = ServicoRecomendacao.gerarLembreteReengajamento(usuario, ignoradas);

        // Se o Zefi ja sugeriu algo hoje e o usuario nao respondeu, repete a mesma sugestao
        // (evita criar registros a cada vez que a tela e aberta e distorcer o engajamento)
        RecomendacaoAvatar pendente = recomendacaoDAO.buscarPendenteDeHoje(cpf).orElse(null);
        Missao missao;
        int recomendacaoId;
        if (pendente != null && missaoDAO.buscarPorId(pendente.getMissaoSugeridaId()).isPresent()) {
            missao = missaoDAO.buscarPorId(pendente.getMissaoSugeridaId()).get();
            recomendacaoId = pendente.getId();
        } else {
            missao = ServicoRecomendacao.gerarRecomendacao(usuario, missaoDAO.listarTodas());
            if (missao == null) {
                return new RecomendacaoResponse(null, null, 0, false,
                        ServicoRecomendacao.gerarMensagemZefi(usuario, null), lembrete);
            }
            recomendacaoId = recomendacaoDAO.registrar(new RecomendacaoAvatar(cpf, missao.getId()));
        }

        return new RecomendacaoResponse(
                recomendacaoId,
                missao,
                ServicoPontos.calcularPontosPorMissao(missao, usuario),
                ServicoPontos.combinaComInteresse(missao, usuario),
                ServicoRecomendacao.gerarMensagemZefi(usuario, missao),
                lembrete);
    }

    public List<RecomendacaoAvatar> historico(String cpf) {
        usuarioBO.buscar(cpf);
        return recomendacaoDAO.listarPorUsuario(cpf, 50);
    }

    /** Remove uma recomendacao do historico do proprio usuario (os pontos ja creditados nao mudam). */
    public void excluir(String cpf, int recomendacaoId) {
        RecomendacaoAvatar rec = recomendacaoDAO.buscarPorId(recomendacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recomendação não encontrada."));
        if (!rec.getCpfUsuario().equals(cpf)) {
            throw new RecursoNaoEncontradoException("Recomendação não encontrada.");
        }
        recomendacaoDAO.deletar(recomendacaoId);
        Auditoria.registrar("RECOMENDACAO_EXCLUIDA", cpf, "recomendacao=" + recomendacaoId);
    }

    public AceiteResponse aceitar(String cpf, int recomendacaoId) {
        RecomendacaoAvatar rec = recomendacaoDAO.buscarPorId(recomendacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recomendação não encontrada."));
        // Garante que um usuario nao aceite recomendacoes de outra pessoa
        if (!rec.getCpfUsuario().equals(cpf)) {
            throw new RecursoNaoEncontradoException("Recomendação não encontrada.");
        }
        if (rec.isAceita()) throw new RegraNegocioException("Essa missão já foi concluída.");

        Usuario usuario = usuarioBO.buscar(cpf);
        Missao missao = missaoDAO.buscarPorId(rec.getMissaoSugeridaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Missão não encontrada."));

        // UPDATE condicional: se duas requisicoes chegarem juntas, so uma credita pontos
        if (!recomendacaoDAO.marcarComoAceita(recomendacaoId)) {
            throw new RegraNegocioException("Essa missão já foi concluída.");
        }
        int pontos = ServicoPontos.calcularPontosPorMissao(missao, usuario);
        int novoSaldo = ServicoPontos.atualizarSaldo(usuario, pontos);
        usuarioDAO.atualizarPontos(cpf, novoSaldo);
        usuario.setPontosAcumulados(novoSaldo);
        Auditoria.registrar("MISSAO_CONCLUIDA", cpf, "recomendacao=" + recomendacaoId + " pontos=+" + pontos);

        return new AceiteResponse(pontos, novoSaldo,
                ServicoRecomendacao.gerarMensagemCelebracao(pontos, novoSaldo),
                ServicoRecomendacao.gerarResumoProgresso(usuario));
    }
}
