package br.com.zefi.resource;

import br.com.zefi.bo.RecomendacaoBO;
import br.com.zefi.dto.AceiteResponse;
import br.com.zefi.dto.RecomendacaoResponse;
import br.com.zefi.model.RecomendacaoAvatar;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{cpf}")
public class RecomendacaoResource {

    private final RecomendacaoBO bo;

    public RecomendacaoResource(RecomendacaoBO bo) {
        this.bo = bo;
    }

    /** Missao que o Zefi recomenda agora, com a fala dele. */
    @GetMapping("/recomendacao")
    public RecomendacaoResponse recomendar(@PathVariable String cpf) {
        return bo.recomendar(cpf);
    }

    @GetMapping("/recomendacoes")
    public List<RecomendacaoAvatar> historico(@PathVariable String cpf) {
        return bo.historico(cpf);
    }

    /** Usuario concluiu a missao recomendada: credita os pontos. */
    @PutMapping("/recomendacoes/{id}/aceitar")
    public AceiteResponse aceitar(@PathVariable String cpf, @PathVariable int id) {
        return bo.aceitar(cpf, id);
    }

    /** Remove uma recomendacao do historico do usuario. */
    @DeleteMapping("/recomendacoes/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String cpf, @PathVariable int id) {
        bo.excluir(cpf, id);
        return ResponseEntity.noContent().build();
    }
}
