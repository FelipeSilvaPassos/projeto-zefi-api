package br.com.zefi.bo;

import br.com.zefi.dao.UsuarioDAO;
import br.com.zefi.dto.ResgateResponse;
import br.com.zefi.exception.RecursoNaoEncontradoException;
import br.com.zefi.exception.RegraNegocioException;
import br.com.zefi.model.Recompensa;
import br.com.zefi.model.Usuario;
import br.com.zefi.service.Auditoria;
import br.com.zefi.service.ServicoPontos;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecompensaBO {

    private final UsuarioDAO usuarioDAO;
    private final UsuarioBO usuarioBO;

    public RecompensaBO(UsuarioDAO usuarioDAO, UsuarioBO usuarioBO) {
        this.usuarioDAO = usuarioDAO;
        this.usuarioBO = usuarioBO;
    }

    public List<Recompensa> catalogo() {
        return ServicoPontos.getCatalogo();
    }

    public ResgateResponse resgatar(String cpf, Integer recompensaId) {
        if (recompensaId == null) throw new RegraNegocioException("Informe a recompensa.");
        Recompensa recompensa = ServicoPontos.buscarRecompensa(recompensaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recompensa não encontrada."));

        Usuario usuario = usuarioBO.buscar(cpf);
        if (!ServicoPontos.verificarElegibilidadeRecompensa(usuario, recompensa.custo())) {
            int faltam = recompensa.custo() - usuario.getPontosAcumulados();
            throw new RegraNegocioException("Você ainda não tem pontos suficientes. Faltam " + faltam + " pontos.");
        }

        int novoSaldo = usuario.getPontosAcumulados() - recompensa.custo();
        usuarioDAO.atualizarPontos(cpf, novoSaldo);
        Auditoria.registrar("RESGATE", cpf, "recompensa=" + recompensa.nome() + " pontos=-" + recompensa.custo());

        return new ResgateResponse(recompensa.nome(), recompensa.custo(), novoSaldo,
                "Recompensa \"" + recompensa.nome() + "\" resgatada com sucesso! 🎉 Saldo restante: " + novoSaldo + " pontos.");
    }
}
