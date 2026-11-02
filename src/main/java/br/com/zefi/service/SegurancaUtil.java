package br.com.zefi.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SegurancaUtil {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private SegurancaUtil() {}

    /** Hash de senha com BCrypt (salt aleatorio embutido). */
    public static String gerarHash(String senha) {
        return BCRYPT.encode(senha);
    }

    /**
     * Confere a senha. Aceita hashes BCrypt (novos) e SHA-256 (usuarios criados
     * na Sprint 3), para nao quebrar contas antigas.
     */
    public static boolean senhaConfere(String senhaDigitada, String hashSalvo) {
        if (senhaDigitada == null || hashSalvo == null) return false;
        if (isBcrypt(hashSalvo)) return BCRYPT.matches(senhaDigitada, hashSalvo);
        return MessageDigest.isEqual(
                gerarHashLegado(senhaDigitada).getBytes(StandardCharsets.UTF_8),
                hashSalvo.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isBcrypt(String hash) {
        return hash != null && hash.startsWith("$2");
    }

    /** SHA-256 usado na Sprint 3. Mantido so para migrar contas antigas. */
    static String gerarHashLegado(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Validacao completa de CPF: 11 digitos, rejeita sequencias repetidas
     * e confere os dois digitos verificadores.
     */
    public static boolean cpfValido(String cpf) {
        if (cpf == null) return false;
        cpf = limparCpf(cpf);
        if (cpf.length() != 11) return false;
        if (cpf.chars().distinct().count() == 1) return false;

        int[] numeros = new int[11];
        for (int i = 0; i < 11; i++) numeros[i] = Character.getNumericValue(cpf.charAt(i));

        int soma = 0;
        for (int i = 0; i < 9; i++) soma += numeros[i] * (10 - i);
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;
        if (digito1 != numeros[9]) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) soma += numeros[i] * (11 - i);
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;
        return digito2 == numeros[10];
    }

    /** Remove qualquer formatacao do CPF, deixando so os numeros. */
    public static String limparCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    }

    /** Mascara o CPF para logs: ***.***.*89-01 (nunca registrar CPF completo). */
    public static String mascararCpf(String cpf) {
        String limpo = limparCpf(cpf);
        if (limpo == null || limpo.length() != 11) return "***";
        return "***.***.*" + limpo.substring(8, 9) + "-" + limpo.substring(9);
    }

    /**
     * Sanitizacao de textos livres: remove tags HTML (evita XSS quando o texto
     * for exibido no site), caracteres de controle e espacos repetidos.
     */
    public static String sanitizar(String texto) {
        if (texto == null) return null;
        return texto.replaceAll("<[^>]*>", "")
                .replaceAll("[\\p{Cntrl}&&[^\\n]]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
