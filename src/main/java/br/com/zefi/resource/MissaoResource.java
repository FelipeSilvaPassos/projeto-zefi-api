package br.com.zefi.resource;

import br.com.zefi.bo.MissaoBO;
import br.com.zefi.model.Missao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Leitura publica; cadastro, edicao e exclusao exigem o cabecalho X-Admin-Key. */
@RestController
@RequestMapping("/api/missoes")
public class MissaoResource {

    private final MissaoBO bo;

    public MissaoResource(MissaoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<Missao> listar(@RequestParam(required = false) String categoria) {
        return bo.listar(categoria);
    }

    @GetMapping("/{id}")
    public Missao buscar(@PathVariable int id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Missao> cadastrar(@RequestBody Missao missao) {
        Missao criada = bo.cadastrar(missao);
        return ResponseEntity.created(URI.create("/api/missoes/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public Missao atualizar(@PathVariable int id, @RequestBody Missao missao) {
        return bo.atualizar(id, missao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
