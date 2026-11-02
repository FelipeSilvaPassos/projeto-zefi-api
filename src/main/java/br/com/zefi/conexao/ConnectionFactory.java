package br.com.zefi.conexao;

import br.com.zefi.exception.PersistenciaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexoes com o Oracle.
 * As credenciais vem de variaveis de ambiente (DB_URL, DB_USER, DB_PASSWORD),
 * para nao ficarem expostas no codigo nem no GitHub.
 * Os valores padrao servem apenas para rodar localmente (Oracle XE no Docker).
 */
public final class ConnectionFactory {

    private static final String URL = env("DB_URL", "jdbc:oracle:thin:@//localhost:1521/XEPDB1");
    private static final String USUARIO = env("DB_USER", "system");
    private static final String SENHA = env("DB_PASSWORD", "ProjetoSOL");

    private ConnectionFactory() {}

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel conectar ao banco de dados", e);
        }
    }

    private static String env(String chave, String padrao) {
        String valor = System.getenv(chave);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}
