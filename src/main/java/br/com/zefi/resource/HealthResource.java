package br.com.zefi.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Verificacao simples de que a API esta no ar (usada pelo Render). */
@RestController
public class HealthResource {
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "servico", "Zefi API");
    }
}
