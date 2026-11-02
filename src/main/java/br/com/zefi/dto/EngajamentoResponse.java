package br.com.zefi.dto;

import br.com.zefi.model.NivelEngajamento;

public record EngajamentoResponse(NivelEngajamento nivel, int acoesUltimos7Dias,
                                  int recomendacoesIgnoradas, String mensagemZefi) {}
