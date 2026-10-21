package br.com.ada.lms.rest.dto;

/** Corpo padrão das respostas de erro: {"status": 404, "erro": "..."} */
public record ErroDTO(int status, String erro) {
}
