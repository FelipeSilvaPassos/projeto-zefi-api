package br.com.zefi.dto;

import br.com.zefi.model.Missao;

public record RecomendacaoResponse(Integer recomendacaoId, Missao missao, int pontosPrevistos,
                                   boolean bonusPersonalizacao, String mensagemZefi, String lembrete) {}
