package com.redia.back.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "dishes")
public class Dish {

    // Id del plato
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Nombre del plato
    @Column(nullable = false)
    private String nombre;

    // Descripción del plato
    @Column(nullable = false)
    private String descripcion;

    // Precio del plato
    @Column(nullable = false)
    private Double precio;

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
    public Dish(String nombre, String descripcion, Double precio, String imageUrl,
            Boolean available) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imageUrl = imageUrl;
        this.available = available;
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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<DishIngredient> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<DishIngredient> ingredientes) {
        this.ingredientes = ingredientes;
    }
}