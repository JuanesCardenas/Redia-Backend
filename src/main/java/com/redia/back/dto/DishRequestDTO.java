package com.redia.back.dto;

/**
 * DTO para crear o editar un plato del menú.
 */
public record DishRequestDTO(
        String nombre,
        String descripcion,
        Double precio,
        String categoria,
        String imageUrl,
        Boolean available
) {
}
