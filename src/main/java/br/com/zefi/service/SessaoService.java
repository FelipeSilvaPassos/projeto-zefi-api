package br.com.zefi.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sessoes de login por token.
 * No login, a API gera um token aleatorio; o front envia em cada requisicao
 * (Authorization: Bearer token) e o filtro descobre qual CPF esta logado.
 * Guardado em memoria: suficiente para o MVP. Com varias instancias, iria para um Redis ou JWT.
 */
@Service
public class SessaoService {

    private static final long HORAS_VALIDADE = 8;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    private record Sessao(String cpf, Instant expiraEm) {}

    /** Dados devolvidos ao front no login. */
    public record NovaSessao(String token, Instant expiraEm) {}

    public NovaSessao criar(String cpf) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiraEm = Instant.now().plus(HORAS_VALIDADE, ChronoUnit.HOURS);
        sessoes.put(token, new Sessao(cpf, expiraEm));
        return new NovaSessao(token, expiraEm);
    }

    /** Devolve o CPF dono do token, se o token existir e nao estiver expirado. */
    public Optional<String> validar(String token) {
        if (token == null) return Optional.empty();
        Sessao sessao = sessoes.get(token);
        if (sessao == null) return Optional.empty();
        if (sessao.expiraEm().isBefore(Instant.now())) {
            sessoes.remove(token);
            return Optional.empty();
        }
        return Optional.of(sessao.cpf());
    }

    public void encerrar(String token) {
        if (token != null) sessoes.remove(token);
    }

    public void encerrarTodasDoUsuario(String cpf) {
        sessoes.values().removeIf(s -> s.cpf().equals(cpf));
    }
}
