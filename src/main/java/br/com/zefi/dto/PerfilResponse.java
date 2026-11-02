package br.com.zefi.dto;

import br.com.zefi.model.Usuario;

public record PerfilResponse(Usuario usuario, int pontosParaProximaRecompensa, String mensagemZefi) {}
