package br.com.zefi.resource;

import br.com.zefi.bo.EngajamentoBO;
import br.com.zefi.bo.UsuarioBO;
import br.com.zefi.dto.EngajamentoResponse;
import br.com.zefi.dto.LoginRequest;
import br.com.zefi.dto.LoginResponse;
import br.com.zefi.dto.PerfilResponse;
import br.com.zefi.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioResource {

    private final UsuarioBO usuarioBO;
    private final EngajamentoBO engajamentoBO;

    public UsuarioResource(UsuarioBO usuarioBO, EngajamentoBO engajamentoBO) {
        this.usuarioBO = usuarioBO;
        this.engajamentoBO = engajamentoBO;
    }

    /** Cadastro (publico). */
    @PostMapping
    public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) {
        Usuario criado = usuarioBO.cadastrar(usuario);
        return ResponseEntity.created(URI.create("/api/usuarios/" + criado.getCpf())).body(criado);
    }

    /** Login com CPF e senha (publico). Devolve o token de sessao. */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest dados) {
        return usuarioBO.login(dados.cpf(), dados.senha());
    }

    /** Encerra a sessao do token enviado. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        usuarioBO.logout(authorization.replaceFirst("(?i)^Bearer ", ""));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cpf}")
    public PerfilResponse perfil(@PathVariable String cpf) {
        return usuarioBO.perfil(cpf);
    }

    @PutMapping("/{cpf}")
    public Usuario atualizar(@PathVariable String cpf, @RequestBody Usuario dados) {
        return usuarioBO.atualizar(cpf, dados);
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> excluir(@PathVariable String cpf) {
        usuarioBO.excluir(cpf);
        return ResponseEntity.noContent().build();
    }

    /** Nivel de engajamento: o front usa para mudar o visual do Zefi. */
    @GetMapping("/{cpf}/engajamento")
    public EngajamentoResponse engajamento(@PathVariable String cpf) {
        return engajamentoBO.calcular(cpf);
    }
}
