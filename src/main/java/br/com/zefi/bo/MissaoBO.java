package br.com.zefi.bo;

import br.com.zefi.dao.MissaoDAO;
import br.com.zefi.exception.RecursoNaoEncontradoException;
import br.com.zefi.exception.RegraNegocioException;
import br.com.zefi.model.Missao;
import br.com.zefi.service.Categorias;
import br.com.zefi.service.SegurancaUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissaoBO {

    private final MissaoDAO dao;

    public MissaoBO(MissaoDAO dao) {
        this.dao = dao;
    }

    public List<Missao> listar(String categoria) {
        if (categoria == null || categoria.isBlank()) return dao.listarTodas();
        String normalizada = Categorias.normalizar(categoria);
        if (normalizada == null) throw new RegraNegocioException("Categoria inválida.");
        return dao.listarPorCategoria(normalizada);
    }

    public Missao buscar(int id) {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Missão " + id + " não encontrada."));
    }

    public Missao cadastrar(Missao missao) {
        validar(missao);
        return dao.cadastrar(missao);
    }

    public Missao atualizar(int id, Missao dados) {
        buscar(id);
        dados.setId(id);
        validar(dados);
        dao.atualizar(dados);
        return buscar(id);
    }

    public void excluir(int id) {
        buscar(id);
        dao.deletar(id); // se ja foi recomendada, o banco bloqueia (HTTP 409)
    }

    private void validar(Missao m) {
        m.setNome(SegurancaUtil.sanitizar(m.getNome()));
        if (m.getNome() == null || m.getNome().length() < 3 || m.getNome().length() > 100) {
            throw new RegraNegocioException("O nome da missão deve ter entre 3 e 100 caracteres.");
        }
        String categoria = Categorias.normalizar(m.getCategoria());
        if (categoria == null) {
            throw new RegraNegocioException("Categoria inválida. Use: " + String.join(", ", Categorias.VALIDAS));
        }
        m.setCategoria(categoria);
        if (m.getPontosRecompensa() < 1 || m.getPontosRecompensa() > 1000) {
            throw new RegraNegocioException("A missão deve valer entre 1 e 1000 pontos.");
        }
    }
}
