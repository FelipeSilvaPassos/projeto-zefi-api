package br.com.zefi.dao;

import br.com.zefi.conexao.ConnectionFactory;
import br.com.zefi.model.AcaoSustentavel;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AcaoSustentavelDAO {

    // CREATE - devolve o id gerado
    public int registrar(AcaoSustentavel acao) {
        String sql = "INSERT INTO acao_sustentavel (cpf_usuario, descricao, categoria_detectada, pontos_atribuidos) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {
            stmt.setString(1, acao.getCpfUsuario());
            stmt.setString(2, acao.getDescricao());
            stmt.setString(3, acao.getCategoriaDetectada());
            stmt.setInt(4, acao.getPontosAtribuidos());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                return chaves.next() ? chaves.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao registrar acao", e);
        }
    }

    // READ - historico do usuario (com limite, para nao devolver volume excessivo)
    public List<AcaoSustentavel> listarPorUsuario(String cpf, int limite) {
        List<AcaoSustentavel> lista = new ArrayList<>();
        String sql = "SELECT * FROM acao_sustentavel WHERE cpf_usuario = ? ORDER BY data_registro DESC FETCH FIRST ? ROWS ONLY";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setInt(2, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao listar acoes", e);
        }
    }

    // Quantas acoes o usuario registrou nos ultimos N dias (usado no engajamento e no antiabuso)
    public int contarDesde(String cpf, int dias) {
        String sql = "SELECT COUNT(*) FROM acao_sustentavel WHERE cpf_usuario = ? AND data_registro >= SYSTIMESTAMP - NUMTODSINTERVAL(?, 'DAY')";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setInt(2, dias);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao contar acoes", e);
        }
    }

    // READ - buscar uma acao pelo id
    public Optional<AcaoSustentavel> buscarPorId(int id) {
        String sql = "SELECT * FROM acao_sustentavel WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao buscar acao", e);
        }
    }

    // UPDATE - nova descricao, com a categoria e os pontos reavaliados
    public void atualizar(int id, String descricao, String categoria, int pontos) {
        String sql = "UPDATE acao_sustentavel SET descricao = ?, categoria_detectada = ?, pontos_atribuidos = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, descricao);
            stmt.setString(2, categoria);
            stmt.setInt(3, pontos);
            stmt.setInt(4, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao atualizar acao", e);
        }
    }

    // DELETE
    public void deletar(int id) {
        String sql = "DELETE FROM acao_sustentavel WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao excluir acao", e);
        }
    }

    private AcaoSustentavel mapear(ResultSet rs) throws SQLException {
        AcaoSustentavel a = new AcaoSustentavel();
        a.setId(rs.getInt("id"));
        a.setCpfUsuario(rs.getString("cpf_usuario"));
        a.setDescricao(rs.getString("descricao"));
        a.setCategoriaDetectada(rs.getString("categoria_detectada"));
        a.setPontosAtribuidos(rs.getInt("pontos_atribuidos"));
        a.setDataRegistro(rs.getTimestamp("data_registro"));
        return a;
    }
}
