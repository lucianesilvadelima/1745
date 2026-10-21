package br.com.ada.lms.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TrilhaRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "cargaHoraria é obrigatória")
        @Positive(message = "cargaHoraria deve ser maior que zero")
        Integer cargaHoraria) {
}
