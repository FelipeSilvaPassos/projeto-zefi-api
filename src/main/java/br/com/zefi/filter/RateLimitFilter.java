package br.com.zefi.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controle de requisicoes: no maximo 100 por minuto por IP (HTTP 429 acima disso).
 * Protege contra abuso e extracao massiva de dados.
 */
@Component
@Order(2)
public class RateLimitFilter implements Filter {

    private static final int LIMITE_POR_MINUTO = 100;
    private final Map<String, long[]> contadores = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String ip = req.getRemoteAddr();
        long minutoAtual = System.currentTimeMillis() / 60_000;

        long[] janela = contadores.compute(ip, (k, v) ->
                (v == null || v[0] != minutoAtual) ? new long[]{minutoAtual, 1} : new long[]{v[0], v[1] + 1});

        if (janela[1] > LIMITE_POR_MINUTO) {
            RespostaErro.enviar((HttpServletResponse) res, 429, "Muitas requisicoes. Aguarde 1 minuto.");
            return;
        }
        chain.doFilter(req, res);
    }
}
