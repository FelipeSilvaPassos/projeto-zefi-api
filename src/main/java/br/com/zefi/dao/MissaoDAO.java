package br.com.zefi.dao;

import br.com.zefi.conexao.ConnectionFactory;
import br.com.zefi.model.Missao;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MissaoDAO {

    // CREATE - devolve a missao com o id gerado pelo banco
    public Missao cadastrar(Missao missao) {
        String sql = "INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {
            stmt.setString(1, missao.getNome());
            stmt.setString(2, missao.getCategoria());
            stmt.setInt(3, missao.getPontosRecompensa());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) missao.setId(chaves.getInt(1));
            }
            return missao;
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao cadastrar missao", e);
        }
    }

    // READ - listar todas
    public List<Missao> listarTodas() {
        List<Missao> missoes = new ArrayList<>();
        String sql = "SELECT * FROM missao ORDER BY id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) missoes.add(mapear(rs));
            return missoes;
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao listar missoes", e);
        }
    }

    // READ - listar por categoria
    public List<Missao> listarPorCategoria(String categoria) {
        List<Missao> missoes = new ArrayList<>();
        String sql = "SELECT * FROM missao WHERE LOWER(categoria) = LOWER(?) ORDER BY id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) missoes.add(mapear(rs));
            }
            return missoes;
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao listar missoes", e);
        }
    }

    // READ - buscar por id
    public Optional<Missao> buscarPorId(int id) {
        String sql = "SELECT * FROM missao WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao buscar missao", e);
        }
    }

    // UPDATE
    public void atualizar(Missao missao) {
        String sql = "UPDATE missao SET nome = ?, categoria = ?, pontos_recompensa = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, missao.getNome());
            stmt.setString(2, missao.getCategoria());
            stmt.setInt(3, missao.getPontosRecompensa());
            stmt.setInt(4, missao.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao atualizar missao", e);
        }
    }

    // DELETE
    public void deletar(int id) {
        String sql = "DELETE FROM missao WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao excluir missao", e);
        }
    }

    private Missao mapear(ResultSet rs) throws SQLException {
        Missao m = new Missao();
        m.setId(rs.getInt("id"));
        m.setNome(rs.getString("nome"));
        m.setCategoria(rs.getString("categoria"));
        m.setPontosRecompensa(rs.getInt("pontos_recompensa"));
        return m;
    }
}
