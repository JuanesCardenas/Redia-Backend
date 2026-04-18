package com.redia.back.dto;

/**
 * DTO para representar un plato del menú disponible.
 */
public record DishResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Double precio,
        String categoria,
        String imageUrl,
        Boolean available
) {
}
