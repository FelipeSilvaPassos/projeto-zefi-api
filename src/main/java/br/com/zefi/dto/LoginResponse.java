package br.com.zefi.dto;

import br.com.zefi.model.Usuario;

/** O front guarda o token e envia no cabecalho: Authorization: Bearer {token} */
public record LoginResponse(String token, String expiraEm, Usuario usuario) {}
