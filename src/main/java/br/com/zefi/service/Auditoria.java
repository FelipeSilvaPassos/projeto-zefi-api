package br.com.zefi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log de auditoria das acoes criticas (login, cadastro, exclusao, pontos, resgates).
 * Registra quem, o que e quando, sempre com CPF mascarado e sem senhas.
 * Em producao, esses logs ficam no painel do Render (ou seriam enviados a um servico de logs).
 */
public final class Auditoria {

    private static final Logger log = LoggerFactory.getLogger("AUDITORIA");

    private Auditoria() {}

    public static void registrar(String evento, String cpf, String detalhe) {
        log.info("evento={} usuario={} detalhe={}", evento, SegurancaUtil.mascararCpf(cpf), detalhe);
    }
}
