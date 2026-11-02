package br.com.zefi.exception;

/** Violacao de PK/UNIQUE/FK no banco (ex.: e-mail duplicado, missao em uso). */
public class ViolacaoIntegridadeException extends PersistenciaException {
    public ViolacaoIntegridadeException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
