package br.com.zefi.dao;

import br.com.zefi.conexao.ConnectionFactory;
import br.com.zefi.model.Usuario;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
public class UsuarioDAO {

    // CREATE - cadastro de usuario (a senha ja chega com hash)
    public void cadastrar(Usuario usuario) {
        String sql = "INSERT INTO usuario (cpf, senha, nome, email, cidade, categoria_interesse) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getCpf());
            stmt.setString(2, usuario.getSenha());
            stmt.setString(3, usuario.getNome());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getCidade());
            stmt.setString(6, usuario.getCategoriaInteresse());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao cadastrar usuario", e);
        }
    }

    // READ - buscar por CPF (inclui o hash da senha, usado apenas no login)
    public Optional<Usuario> buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM usuario WHERE cpf = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao buscar usuario", e);
        }
    }

    public boolean emailEmUso(String email, String ignorarCpf) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE LOWER(email) = LOWER(?) AND cpf <> ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, ignorarCpf == null ? "-" : ignorarCpf);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao verificar e-mail", e);
        }
    }

    // UPDATE - saldo de pontos
    public void atualizarPontos(String cpf, int novosPontos) {
        String sql = "UPDATE usuario SET pontos_acumulados = ? WHERE cpf = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novosPontos);
            stmt.setString(2, cpf);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao atualizar pontos", e);
        }
    }

    // UPDATE - migra senhas antigas (SHA-256 da Sprint 3) para BCrypt no primeiro login
    public void atualizarSenha(String cpf, String novoHash) {
        String sql = "UPDATE usuario SET senha = ? WHERE cpf = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoHash);
            stmt.setString(2, cpf);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao atualizar senha", e);
        }
    }

    // UPDATE - dados cadastrais
    public void atualizarDados(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, email = ?, cidade = ?, categoria_interesse = ? WHERE cpf = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getCidade());
            stmt.setString(4, usuario.getCategoriaInteresse());
            stmt.setString(5, usuario.getCpf());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao atualizar dados", e);
        }
    }

    /**
     * DELETE - exclui a conta e o historico do usuario numa unica transacao
     * (acoes e recomendacoes dependem do usuario pela chave estrangeira).
     */
    public void deletar(String cpf) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement acoes = conn.prepareStatement("DELETE FROM acao_sustentavel WHERE cpf_usuario = ?");
                 PreparedStatement recs = conn.prepareStatement("DELETE FROM recomendacao_avatar WHERE cpf_usuario = ?");
                 PreparedStatement usuario = conn.prepareStatement("DELETE FROM usuario WHERE cpf = ?")) {
                acoes.setString(1, cpf);
                acoes.executeUpdate();
                recs.setString(1, cpf);
                recs.executeUpdate();
                usuario.setString(1, cpf);
                usuario.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DaoUtil.traduzir("Erro ao excluir usuario", e);
        }
    }

    public boolean cpfJaExiste(String cpf) {
        return buscarPorCpf(cpf).isPresent();
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setCpf(rs.getString("cpf"));
        u.setSenha(rs.getString("senha"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setCidade(rs.getString("cidade"));
        u.setPontosAcumulados(rs.getInt("pontos_acumulados"));
        u.setCategoriaInteresse(rs.getString("categoria_interesse"));
        return u;
    }
}
