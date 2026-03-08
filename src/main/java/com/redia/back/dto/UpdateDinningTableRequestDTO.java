package com.redia.back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateDinningTableRequestDTO(

        @NotBlank(message = "El nombre de la mesa es requerido") String nombre,

        @Min(value = 1, message = "La capacidad debe ser mayor a 0") int capacidad

) {
}
