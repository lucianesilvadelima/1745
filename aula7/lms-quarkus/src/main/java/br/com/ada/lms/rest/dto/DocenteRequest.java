package br.com.ada.lms.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocenteRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(min = 3, max = 100, message = "nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        @Size(max = 150, message = "email deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "especialidade é obrigatória")
        @Size(max = 100, message = "especialidade deve ter no máximo 100 caracteres")
        String especialidade) {
}
