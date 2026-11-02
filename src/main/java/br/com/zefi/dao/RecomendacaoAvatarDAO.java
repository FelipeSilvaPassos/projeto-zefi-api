package br.com.zefi.dao;

import br.com.zefi.conexao.ConnectionFactory;
import br.com.zefi.model.RecomendacaoAvatar;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RecomendacaoAvatarDAO {

    // CREATE - registra que o Zefi sugeriu uma missao ao usuario (devolve o id)
    public int registrar(RecomendacaoAvatar recomendacao) {
        String sql = "INSERT INTO recomendacao_avatar (cpf_usuario, missao_sugerida_id, aceita) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {
            stmt.setString(1, recomendacao.getCpfUsuario());
            stmt.setInt(2, recomendacao.getMissaoSugeridaId());
            stmt.setInt(3, recomendacao.isAceita() ? 1 : 0);
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                return chaves.next() ? chaves.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao registrar recomendacao", e);
        }
    }

    // READ - historico de recomendacoes do usuario
    public List<RecomendacaoAvatar> listarPorUsuario(String cpf, int limite) {
        List<RecomendacaoAvatar> lista = new ArrayList<>();
        String sql = "SELECT * FROM recomendacao_avatar WHERE cpf_usuario = ? ORDER BY data_sugestao DESC FETCH FIRST ? ROWS ONLY";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setInt(2, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao listar recomendacoes", e);
        }
    }

    // READ - buscar por id
    public Optional<RecomendacaoAvatar> buscarPorId(int id) {
        String sql = "SELECT * FROM recomendacao_avatar WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao buscar recomendacao", e);
        }
    }

    // READ - recomendacao pendente feita hoje (evita registrar a mesma sugestao varias vezes)
    public Optional<RecomendacaoAvatar> buscarPendenteDeHoje(String cpf) {
        String sql = "SELECT * FROM recomendacao_avatar WHERE cpf_usuario = ? AND aceita = 0 "
                + "AND data_sugestao >= TRUNC(SYSDATE) ORDER BY data_sugestao DESC FETCH FIRST 1 ROWS ONLY";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao buscar recomendacao pendente", e);
        }
    }

    /**
     * UPDATE - marca como aceita. A condicao "aceita = 0" garante que a mesma
     * recomendacao nao gere pontos duas vezes. Retorna true se atualizou.
     */
    public boolean marcarComoAceita(int id) {
        String sql = "UPDATE recomendacao_avatar SET aceita = 1 WHERE id = ? AND aceita = 0";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao atualizar recomendacao", e);
        }
    }

    // Quantas recomendacoes o usuario recebeu e nunca aceitou (sinal de baixo engajamento)
    public int contarRecomendacoesNaoAceitas(String cpf) {
        String sql = "SELECT COUNT(*) FROM recomendacao_avatar WHERE cpf_usuario = ? AND aceita = 0";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao contar recomendacoes", e);
        }
    }

    // DELETE - remove uma recomendacao do historico
    public void deletar(int id) {
        String sql = "DELETE FROM recomendacao_avatar WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao excluir recomendacao", e);
        }
    }

    private RecomendacaoAvatar mapear(ResultSet rs) throws SQLException {
        RecomendacaoAvatar r = new RecomendacaoAvatar();
        r.setId(rs.getInt("id"));
        r.setCpfUsuario(rs.getString("cpf_usuario"));
        r.setMissaoSugeridaId(rs.getInt("missao_sugerida_id"));
        r.setDataSugestao(rs.getTimestamp("data_sugestao"));
        r.setAceita(rs.getInt("aceita") == 1);
        return r;
    }
}
