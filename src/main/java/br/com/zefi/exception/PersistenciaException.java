package br.com.zefi.exception;

/** Erro ao acessar o banco. A mensagem tecnica fica so no log do servidor. */
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
