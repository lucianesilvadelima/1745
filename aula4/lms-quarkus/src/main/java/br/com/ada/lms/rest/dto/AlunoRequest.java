package br.com.ada.lms.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados que o cliente envia no corpo do POST/PUT/PATCH.
 * Repare que não tem "id": quem gera o id é o servidor.
 *
 * As anotações de validação só são verificadas quando o parâmetro tem @Valid
 * (POST e PUT). Se falharem, o Quarkus responde 400 Bad Request sozinho.
 */
public record AlunoRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        String email) {
}
