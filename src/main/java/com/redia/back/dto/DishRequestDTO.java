package com.redia.back.dto;

/**
 * DTO para crear o editar un plato del menú.
 * Nota: La imagen se recibe aparte como MultipartFile.
 */
public record DishRequestDTO(
        String nombre,
        String descripcion,
        Double precio,
        Boolean available
) {
}
