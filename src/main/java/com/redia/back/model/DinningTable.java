package com.redia.back.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa una mesa dentro del restaurante.
 */
@Entity
@Table(name = "dinning_tables")
public class DinningTable {

    /**
     * Identificador único de la mesa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Nombre o identificador visible de la mesa.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Cantidad de personas que pueden sentarse en la mesa.
     * Por defecto la capacidad es 4.
     */
    @Column(nullable = false)
    private int capacidad = 4;

    /**
     * Estado actual de la mesa.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DinningTableStatus estadoMesa;

    /**
     * Constructor vacío requerido por JPA.
     */
    public DinningTable() {
    }

    /**
     * Constructor con parámetros para crear una mesa
     * con sus datos iniciales.
     */
    public DinningTable(String nombre, int capacidad, DinningTableStatus estadoMesa) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.estadoMesa = estadoMesa;
    }

    // ========================
    // Getters y Setters
    // ========================

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

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public DinningTableStatus getEstadoMesa() {
        return estadoMesa;
    }

    public void setEstadoMesa(DinningTableStatus estadoMesa) {
        this.estadoMesa = estadoMesa;
    }
}