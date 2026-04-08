package com.redia.back.dto;

public record TableAvailabilityDTO(
        String id,
        String nombre,
        int capacidad,
        boolean disponible,
        String estado
) {
}
