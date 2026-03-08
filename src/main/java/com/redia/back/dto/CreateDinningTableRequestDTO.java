package com.redia.back.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDinningTableRequestDTO(

                @NotBlank(message = "El nombre de la mesa es requerido") String nombre,

                @NotBlank(message = "La capacidad de la mesa es requerida") String capacidad

) {
}
