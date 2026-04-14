package com.redia.back.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "ingredients")
public class Ingredient {

    // Id del ingrediente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    // Nombre del ingrediente
    @Column(nullable = false)
    private String nombre;

    // Descripción del ingrediente
    @Column(nullable = false)
    private String descripcion;

    // Categoría del ingrediente
    @Column(nullable = false)
    private String categoria;

    // Stock del ingrediente
    @Column(nullable = false)
    private int stock;

    /*
     * Relación con los platos
     * 
     * @OneToMany(mappedBy = "ingredient")
     * private List<DishIngredient> dishes;
     */

    // Constructor vacío
    public Ingredient() {
    }

    // Constructor
    public Ingredient(String nombre, String descripcion, String categoria, int stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.stock = stock;
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}