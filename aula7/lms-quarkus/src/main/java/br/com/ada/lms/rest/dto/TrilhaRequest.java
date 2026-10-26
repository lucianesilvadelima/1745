package br.com.ada.lms.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TrilhaRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(min = 3, max = 100, message = "nome deve ter entre 3 e 100 caracteres")
        String nome,

        @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "cargaHoraria é obrigatória")
        @Positive(message = "cargaHoraria deve ser maior que zero")
        @Max(value = 1000, message = "cargaHoraria deve ser no máximo 1000 horas")
        Integer cargaHoraria) {
}
