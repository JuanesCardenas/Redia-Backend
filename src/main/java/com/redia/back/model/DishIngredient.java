package com.redia.back.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dish_ingredients")
public class DishIngredient {

    // Id de la relación
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plato al que pertenece el ingrediente
    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    // Ingrediente asociado
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    // Cantidad de ese ingrediente en el plato
    @Column(nullable = false)
    private int cantidad;

    // Constructor vacío
    public DishIngredient() {
    }

    // Constructor
    public DishIngredient(Dish dish, Ingredient ingredient, int cantidad) {
        this.dish = dish;
        this.ingredient = ingredient;
        this.cantidad = cantidad;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public Dish getDish() {
        return dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCantidad() {
        return cantidad;
    }
}