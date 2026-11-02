package br.com.zefi.bo;

import br.com.zefi.dao.UsuarioDAO;
import br.com.zefi.dto.LoginResponse;
import br.com.zefi.dto.PerfilResponse;
import br.com.zefi.exception.NaoAutorizadoException;
import br.com.zefi.exception.RecursoNaoEncontradoException;
import br.com.zefi.exception.RegraNegocioException;
import br.com.zefi.model.Usuario;
import br.com.zefi.service.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Regras de negocio de usuarios: cadastro, login, perfil, atualizacao e exclusao. */
@Service
public class UsuarioBO {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final int SENHA_MINIMA = 8;
    private static final int MAX_TENTATIVAS_LOGIN = 5;
    private static final int MINUTOS_BLOQUEIO = 15;

    private final UsuarioDAO dao;
    private final SessaoService sessaoService;

    // CPF -> [tentativas falhas, instante do primeiro erro] (protecao contra forca bruta)
    private final Map<String, long[]> tentativasFalhas = new ConcurrentHashMap<>();

    public UsuarioBO(UsuarioDAO dao, SessaoService sessaoService) {
        this.dao = dao;
        this.sessaoService = sessaoService;
    }

    public Usuario cadastrar(Usuario novo) {
        String cpf = SegurancaUtil.limparCpf(novo.getCpf());
        if (!SegurancaUtil.cpfValido(cpf)) throw new RegraNegocioException("CPF inválido.");
        if (dao.cpfJaExiste(cpf)) throw new RegraNegocioException("Já existe uma conta com esse CPF.");
        if (novo.getSenha() == null || novo.getSenha().length() < SENHA_MINIMA) {
            throw new RegraNegocioException("A senha deve ter pelo menos " + SENHA_MINIMA + " caracteres.");
        }
        novo.setCpf(cpf);
        validarDados(novo);
        if (dao.emailEmUso(novo.getEmail(), null)) throw new RegraNegocioException("E-mail já cadastrado.");

        novo.setSenha(SegurancaUtil.gerarHash(novo.getSenha()));
        dao.cadastrar(novo);
        Auditoria.registrar("CADASTRO", cpf, "conta criada");
        return buscar(cpf);
    }

    public LoginResponse login(String cpfDigitado, String senha) {
        String cpf = SegurancaUtil.limparCpf(cpfDigitado);
        if (!SegurancaUtil.cpfValido(cpf)) throw new NaoAutorizadoException("CPF ou senha incorretos.");
        verificarBloqueio(cpf);

        Usuario usuario = dao.buscarPorCpf(cpf).orElse(null);
        // Mesma mensagem para CPF inexistente e senha errada: nao revela quais CPFs tem conta
        if (usuario == null || !SegurancaUtil.senhaConfere(senha, usuario.getSenha())) {
            registrarFalha(cpf);
            Auditoria.registrar("LOGIN_FALHOU", cpf, "credenciais invalidas");
            throw new NaoAutorizadoException("CPF ou senha incorretos.");
        }
        tentativasFalhas.remove(cpf);

        // Conta antiga (SHA-256 da Sprint 3): troca para BCrypt de forma transparente
        if (!SegurancaUtil.isBcrypt(usuario.getSenha())) {
            dao.atualizarSenha(cpf, SegurancaUtil.gerarHash(senha));
        }

        SessaoService.NovaSessao sessao = sessaoService.criar(cpf);
        Auditoria.registrar("LOGIN", cpf, "sessao iniciada");
        return new LoginResponse(sessao.token(), sessao.expiraEm().toString(), usuario);
    }

    public void logout(String token) {
        sessaoService.encerrar(token);
    }

    public Usuario buscar(String cpf) {
        return dao.buscarPorCpf(cpf)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    public PerfilResponse perfil(String cpf) {
        Usuario usuario = buscar(cpf);
        return new PerfilResponse(usuario,
                ServicoPontos.pontosFaltantesProximaRecompensa(usuario),
                ServicoRecomendacao.gerarResumoProgresso(usuario));
    }

    public Usuario atualizar(String cpf, Usuario dados) {
        Usuario atual = buscar(cpf);
        // So estes campos podem ser alterados; CPF, senha e pontos nao mudam por aqui
        atual.setNome(dados.getNome());
        atual.setEmail(dados.getEmail());
        atual.setCidade(dados.getCidade());
        atual.setCategoriaInteresse(dados.getCategoriaInteresse());
        validarDados(atual);
        if (dao.emailEmUso(atual.getEmail(), cpf)) throw new RegraNegocioException("E-mail já cadastrado para outra conta.");

        dao.atualizarDados(atual);
        Auditoria.registrar("ATUALIZACAO_CADASTRO", cpf, "dados alterados");
        return buscar(cpf);
    }

    public void excluir(String cpf) {
        buscar(cpf);
        dao.deletar(cpf);
        sessaoService.encerrarTodasDoUsuario(cpf);
        Auditoria.registrar("EXCLUSAO_CONTA", cpf, "conta e historico removidos");
    }

    /** Validacao e sanitizacao dos campos cadastrais. */
    private void validarDados(Usuario u) {
        u.setNome(SegurancaUtil.sanitizar(u.getNome()));
        u.setCidade(SegurancaUtil.sanitizar(u.getCidade()));
        u.setEmail(u.getEmail() == null ? null : u.getEmail().trim().toLowerCase());

        if (u.getNome() == null || u.getNome().length() < 3 || u.getNome().length() > 100) {
            throw new RegraNegocioException("O nome deve ter entre 3 e 100 caracteres.");
        }
        if (u.getEmail() == null || u.getEmail().length() > 150 || !EMAIL.matcher(u.getEmail()).matches()) {
            throw new RegraNegocioException("E-mail inválido.");
        }
        if (u.getCidade() == null || u.getCidade().isBlank() || u.getCidade().length() > 100) {
            throw new RegraNegocioException("Informe a cidade (até 100 caracteres).");
        }
        String categoria = Categorias.normalizar(u.getCategoriaInteresse());
        if (categoria == null) {
            throw new RegraNegocioException("Categoria inválida. Use: " + String.join(", ", Categorias.VALIDAS));
        }
        u.setCategoriaInteresse(categoria);
    }

    private void verificarBloqueio(String cpf) {
        long[] registro = tentativasFalhas.get(cpf);
        if (registro == null) return;
        Instant inicio = Instant.ofEpochMilli(registro[1]);
        if (inicio.plus(MINUTOS_BLOQUEIO, ChronoUnit.MINUTES).isBefore(Instant.now())) {
            tentativasFalhas.remove(cpf);
        } else if (registro[0] >= MAX_TENTATIVAS_LOGIN) {
            Auditoria.registrar("LOGIN_BLOQUEADO", cpf, "excesso de tentativas");
            throw new NaoAutorizadoException("Muitas tentativas. Tente novamente em " + MINUTOS_BLOQUEIO + " minutos.");
        }
    }

    private void registrarFalha(String cpf) {
        tentativasFalhas.compute(cpf, (k, v) ->
                v == null ? new long[]{1, System.currentTimeMillis()} : new long[]{v[0] + 1, v[1]});
    }
}
