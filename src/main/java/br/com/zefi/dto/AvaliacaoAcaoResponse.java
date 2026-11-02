package br.com.zefi.dto;

public record AvaliacaoAcaoResponse(int acaoId, String categoria, int pontos, int novoSaldo,
                                    String mensagemZefi, String progresso) {}
