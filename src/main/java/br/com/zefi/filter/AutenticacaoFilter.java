package br.com.zefi.filter;

import br.com.zefi.service.Auditoria;
import br.com.zefi.service.SessaoService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Controle de acesso:
 *  - Rotas publicas: health, cadastro, login, leitura de missoes e catalogo.
 *  - Rotas /api/usuarios/{cpf}/...: exigem token valido E o token precisa ser do
 *    proprio CPF (um usuario nunca acessa dados de outro).
 *  - Escrita em /api/missoes: exige o cabecalho X-Admin-Key (perfil administrador).
 */
@Component
@Order(3)
public class AutenticacaoFilter implements Filter {

    private static final String ADMIN_KEY = System.getenv().getOrDefault("ADMIN_KEY", "zefi-admin-local");

    private final SessaoService sessaoService;

    public AutenticacaoFilter(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String metodo = request.getMethod();
        String caminho = request.getRequestURI().substring(request.getContextPath().length());

        // ---------- Rotas publicas ----------
        boolean publica = caminho.equals("/api/health")
                || (metodo.equals("POST") && (caminho.equals("/api/usuarios") || caminho.equals("/api/usuarios/login")))
                || (metodo.equals("GET") && (caminho.startsWith("/api/missoes") || caminho.equals("/api/recompensas")));
        if (publica) {
            chain.doFilter(req, res);
            return;
        }

        // ---------- Escrita de missoes: somente administrador ----------
        if (caminho.startsWith("/api/missoes")) {
            if (!chaveAdminValida(request.getHeader("X-Admin-Key"))) {
                Auditoria.registrar("ACESSO_NEGADO_ADMIN", null, metodo + " " + caminho);
                RespostaErro.enviar(response, 403, "Acesso restrito a administradores.");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        // ---------- Rotas do usuario: exigem token ----------
        if (caminho.startsWith("/api/usuarios/")) {
            Optional<String> cpfLogado = sessaoService.validar(extrairToken(request));
            if (cpfLogado.isEmpty()) {
                RespostaErro.enviar(response, 401, "Faca login para continuar.");
                return;
            }
            if (!caminho.equals("/api/usuarios/logout")) {
                String cpfDaRota = caminho.split("/")[3];
                if (!cpfDaRota.equals(cpfLogado.get())) {
                    Auditoria.registrar("ACESSO_NEGADO", cpfLogado.get(), "tentou acessar dados de outro usuario");
                    RespostaErro.enviar(response, 403, "Voce nao tem permissao para acessar esses dados.");
                    return;
                }
            }
        }
        chain.doFilter(req, res);
    }

    private String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        return cabecalho.substring(7).trim();
    }

    /** Comparacao em tempo constante (evita descobrir a chave medindo o tempo de resposta). */
    private boolean chaveAdminValida(String chave) {
        if (chave == null) return false;
        return MessageDigest.isEqual(chave.getBytes(StandardCharsets.UTF_8), ADMIN_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
