package com.redia.back.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "menus")
public class Menu {

    // Id del menú
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del menú
    @Column(nullable = false)
    private String nombre;

    // Indica si el menú está activo
    @Column(nullable = false)
    private Boolean activo = true;

    // Platos disponibles en el menú
    @OneToMany
    @JoinColumn(name = "menu_id")
    private List<Dish> dishes;

    // Constructor vacío
    public Menu() {
    }

    // Constructor
    public Menu(String nombre, Boolean activo) {
        this.nombre = nombre;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }
}