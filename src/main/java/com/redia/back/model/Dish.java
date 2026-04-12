package com.redia.back.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "dishes")
public class Dish {

    // Id del plato
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del plato
    @Column(nullable = false)
    private String nombre;

    // Descripción del plato
    @Column(nullable = false)
    private String descripcion;

    // Precio del plato
    @Column(nullable = false)
    private Double precio;

    // Categoría del plato
    @Column(nullable = false)
    private String categoria;

    // URL de la imagen del plato
    @Column(nullable = false)
    private String imageUrl;

    // Indica si el plato está disponible
    @Column(nullable = false)
    private Boolean available = true;

    // Lista de ingredientes del plato (con cantidad)
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DishIngredient> ingredientes;

    // Constructor vacío
    public Dish() {
    }

    // Constructor
    public Dish(String nombre, String descripcion, Double precio, String categoria, String imageUrl,
            Boolean available) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getAvailable() {
        return available;
    }

    public List<DishIngredient> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<DishIngredient> ingredientes) {
        this.ingredientes = ingredientes;
    }
}