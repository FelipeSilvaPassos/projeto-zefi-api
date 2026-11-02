package br.com.zefi.dao;

import br.com.zefi.exception.PersistenciaException;
import br.com.zefi.exception.ViolacaoIntegridadeException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Converte erros do JDBC em excecoes da aplicacao. */
final class DaoUtil {

    // ORA-00001 (unique), ORA-02291 (FK sem pai), ORA-02292 (registro com filhos)
    private static final int ORA_UNIQUE = 1;
    private static final int ORA_FK_PAI = 2291;
    private static final int ORA_FK_FILHO = 2292;

    private DaoUtil() {}

    static PersistenciaException traduzir(String contexto, SQLException e) {
        int codigo = e.getErrorCode();
        if (e instanceof SQLIntegrityConstraintViolationException
                || codigo == ORA_UNIQUE || codigo == ORA_FK_PAI || codigo == ORA_FK_FILHO) {
            String mensagem = switch (codigo) {
                case ORA_UNIQUE -> "Ja existe um registro com esses dados (CPF ou e-mail duplicado).";
                case ORA_FK_FILHO -> "Este registro esta em uso e nao pode ser excluido.";
                case ORA_FK_PAI -> "Registro relacionado nao encontrado.";
                default -> "Os dados violam uma regra do banco.";
            };
            return new ViolacaoIntegridadeException(mensagem, e);
        }
        return new PersistenciaException(contexto, e);
    }
}
