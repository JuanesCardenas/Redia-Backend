package com.redia.back.dto;

/**
 * DTO para representar un plato del menú disponible.
 */
public record DishResponseDTO(
        String id,
        String nombre,
        String descripcion,
        Double precio,
        String imageUrl,
        Boolean available
) {
}
