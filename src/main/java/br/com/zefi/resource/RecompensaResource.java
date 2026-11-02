package br.com.zefi.resource;

import br.com.zefi.bo.RecompensaBO;
import br.com.zefi.dto.ResgateRequest;
import br.com.zefi.dto.ResgateResponse;
import br.com.zefi.model.Recompensa;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecompensaResource {

    private final RecompensaBO bo;

    public RecompensaResource(RecompensaBO bo) {
        this.bo = bo;
    }

    /** Catalogo de recompensas (publico). */
    @GetMapping("/recompensas")
    public List<Recompensa> catalogo() {
        return bo.catalogo();
    }

    @PostMapping("/usuarios/{cpf}/resgates")
    public ResgateResponse resgatar(@PathVariable String cpf, @RequestBody ResgateRequest dados) {
        return bo.resgatar(cpf, dados.recompensaId());
    }
}
