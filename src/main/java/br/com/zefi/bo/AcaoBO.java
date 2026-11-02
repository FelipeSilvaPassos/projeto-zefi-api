package br.com.zefi.bo;

import br.com.zefi.dao.AcaoSustentavelDAO;
import br.com.zefi.dao.UsuarioDAO;
import br.com.zefi.dto.AvaliacaoAcaoResponse;
import br.com.zefi.exception.RecursoNaoEncontradoException;
import br.com.zefi.exception.RegraNegocioException;
import br.com.zefi.model.AcaoSustentavel;
import br.com.zefi.model.Usuario;
import br.com.zefi.service.*;
import org.springframework.stereotype.Service;

import java.util.List;

/** Chat com o Zefi: o usuario relata uma acao e o Zefi avalia e da os pontos. */
@Service
public class AcaoBO {

    private static final int TAMANHO_MINIMO = 5;
    private static final int TAMANHO_MAXIMO = 500;
    private static final int LIMITE_ACOES_POR_DIA = 10; // evita farmar pontos com mensagens repetidas

    private final AcaoSustentavelDAO acaoDAO;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioBO usuarioBO;

    public AcaoBO(AcaoSustentavelDAO acaoDAO, UsuarioDAO usuarioDAO, UsuarioBO usuarioBO) {
        this.acaoDAO = acaoDAO;
        this.usuarioDAO = usuarioDAO;
        this.usuarioBO = usuarioBO;
    }

    public AvaliacaoAcaoResponse registrar(String cpf, String descricaoRecebida) {
        String descricao = validarDescricao(descricaoRecebida);
        if (acaoDAO.contarDesde(cpf, 1) >= LIMITE_ACOES_POR_DIA) {
            Auditoria.registrar("LIMITE_ACOES", cpf, "limite diario atingido");
            throw new RegraNegocioException("Você já registrou " + LIMITE_ACOES_POR_DIA
                    + " ações nas últimas 24 horas. Volte amanhã! 🍃");
        }

        Usuario usuario = usuarioBO.buscar(cpf);
        ServicoAvaliacaoAcao.ResultadoAvaliacao resultado = ServicoAvaliacaoAcao.avaliar(descricao, usuario);

        int acaoId = acaoDAO.registrar(new AcaoSustentavel(cpf, descricao, resultado.categoria(), resultado.pontos()));
        int novoSaldo = ServicoPontos.atualizarSaldo(usuario, resultado.pontos());
        usuarioDAO.atualizarPontos(cpf, novoSaldo);
        usuario.setPontosAcumulados(novoSaldo);
        Auditoria.registrar("ACAO_REGISTRADA", cpf, "acao=" + acaoId + " pontos=+" + resultado.pontos());

        return new AvaliacaoAcaoResponse(acaoId, resultado.categoria(), resultado.pontos(), novoSaldo,
                resultado.mensagemZefi(), ServicoRecomendacao.gerarResumoProgresso(usuario));
    }

    public List<AcaoSustentavel> historico(String cpf, Integer limite) {
        int lim = limite == null ? 20 : limite;
        if (lim < 1 || lim > 50) throw new RegraNegocioException("O limite deve estar entre 1 e 50.");
        usuarioBO.buscar(cpf);
        return acaoDAO.listarPorUsuario(cpf, lim);
    }

    /** Busca uma acao garantindo que ela pertence ao usuario informado. */
    public AcaoSustentavel buscar(String cpf, int id) {
        usuarioBO.buscar(cpf);
        AcaoSustentavel acao = acaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ação não encontrada."));
        if (!acao.getCpfUsuario().equals(cpf)) {
            throw new RecursoNaoEncontradoException("Ação não encontrada.");
        }
        return acao;
    }

    /**
     * Corrige a descricao da acao: o Zefi reavalia categoria e pontos, e o saldo do usuario
     * e ajustado pela diferenca (nunca fica negativo).
     */
    public AcaoSustentavel atualizar(String cpf, int id, String descricaoRecebida) {
        AcaoSustentavel atual = buscar(cpf, id);
        String descricao = validarDescricao(descricaoRecebida);

        Usuario usuario = usuarioBO.buscar(cpf);
        ServicoAvaliacaoAcao.ResultadoAvaliacao resultado = ServicoAvaliacaoAcao.avaliar(descricao, usuario);
        int diferenca = resultado.pontos() - atual.getPontosAtribuidos();
        int novoSaldo = Math.max(0, usuario.getPontosAcumulados() + diferenca);

        acaoDAO.atualizar(id, descricao, resultado.categoria(), resultado.pontos());
        usuarioDAO.atualizarPontos(cpf, novoSaldo);
        Auditoria.registrar("ACAO_ATUALIZADA", cpf, "acao=" + id + " pontos=" + (diferenca >= 0 ? "+" : "") + diferenca);
        return buscar(cpf, id);
    }

    /** Exclui a acao e estorna os pontos que ela tinha rendido (o saldo nunca fica negativo). */
    public void excluir(String cpf, int id) {
        AcaoSustentavel atual = buscar(cpf, id);
        Usuario usuario = usuarioBO.buscar(cpf);
        int novoSaldo = Math.max(0, usuario.getPontosAcumulados() - atual.getPontosAtribuidos());

        acaoDAO.deletar(id);
        usuarioDAO.atualizarPontos(cpf, novoSaldo);
        Auditoria.registrar("ACAO_EXCLUIDA", cpf, "acao=" + id + " pontos=-" + atual.getPontosAtribuidos());
    }

    private String validarDescricao(String descricaoRecebida) {
        String descricao = SegurancaUtil.sanitizar(descricaoRecebida);
        if (descricao == null || descricao.length() < TAMANHO_MINIMO || descricao.length() > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("Descreva sua ação em " + TAMANHO_MINIMO + " a " + TAMANHO_MAXIMO + " caracteres.");
        }
        return descricao;
    }
}
