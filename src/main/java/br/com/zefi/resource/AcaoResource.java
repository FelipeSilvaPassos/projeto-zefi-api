package br.com.zefi.resource;

import br.com.zefi.bo.AcaoBO;
import br.com.zefi.dto.AcaoRequest;
import br.com.zefi.dto.AvaliacaoAcaoResponse;
import br.com.zefi.model.AcaoSustentavel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{cpf}/acoes")
public class AcaoResource {

    private final AcaoBO bo;

    public AcaoResource(AcaoBO bo) {
        this.bo = bo;
    }

    /** Chat com o Zefi: o usuario conta o que fez e recebe a avaliacao e os pontos. */
    @PostMapping
    public ResponseEntity<AvaliacaoAcaoResponse> registrar(@PathVariable String cpf, @RequestBody AcaoRequest dados) {
        AvaliacaoAcaoResponse resposta = bo.registrar(cpf, dados.descricao());
        return ResponseEntity.created(URI.create("/api/usuarios/" + cpf + "/acoes/" + resposta.acaoId())).body(resposta);
    }

    @GetMapping
    public List<AcaoSustentavel> historico(@PathVariable String cpf, @RequestParam(required = false) Integer limite) {
        return bo.historico(cpf, limite);
    }

    @GetMapping("/{id}")
    public AcaoSustentavel buscar(@PathVariable String cpf, @PathVariable int id) {
        return bo.buscar(cpf, id);
    }

    /** Corrige a descricao; o Zefi reavalia categoria e pontos. */
    @PutMapping("/{id}")
    public AcaoSustentavel atualizar(@PathVariable String cpf, @PathVariable int id, @RequestBody AcaoRequest dados) {
        return bo.atualizar(cpf, id, dados.descricao());
    }

    /** Exclui a acao e estorna os pontos. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String cpf, @PathVariable int id) {
        bo.excluir(cpf, id);
        return ResponseEntity.noContent().build();
    }
}
