package br.com.zefi.filter;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Escreve respostas de erro em JSON direto nos filtros (antes de chegar nos Resources). */
final class RespostaErro {

    private RespostaErro() {}

    static void enviar(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"mensagem\":\"" + mensagem + "\"}");
    }
}
