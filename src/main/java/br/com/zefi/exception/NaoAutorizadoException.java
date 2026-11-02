package br.com.zefi.exception;

/** Credenciais invalidas (HTTP 401). */
public class NaoAutorizadoException extends RuntimeException {
    public NaoAutorizadoException(String mensagem) { super(mensagem); }
}
