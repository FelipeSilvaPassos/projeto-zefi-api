package br.com.zefi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada da API do Zefi (substitui o menu de console da Sprint 3). */
@SpringBootApplication
public class ZefiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZefiApplication.class, args);
    }
}
