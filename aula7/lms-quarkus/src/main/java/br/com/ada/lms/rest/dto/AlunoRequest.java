package br.com.ada.lms.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados que o cliente envia no corpo do POST/PUT/PATCH.
 * Repare que não tem "id": quem gera o id é o servidor.
 *
 * As anotações de validação só são verificadas quando o parâmetro tem @Valid
 * (POST e PUT). Se falharem, o Quarkus responde 400 Bad Request sozinho.
 */
public record AlunoRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(min = 3, max = 100, message = "nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        @Size(max = 150, message = "email deve ter no máximo 150 caracteres")
        String email) {
}
