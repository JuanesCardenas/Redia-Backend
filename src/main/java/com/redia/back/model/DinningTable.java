package com.redia.back.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa una mesa dentro del restaurante.
 * Las mesas son fijas (predeterminadas) y tienen un ID textual "1"-"10".
 */
@Entity
@Table(name = "dinning_tables")
public class DinningTable {

    /**
     * Identificador único de la mesa (valor textual fijo: "1" a "10").
     * No se genera automáticamente; se asigna al inicializar el sistema.
     */
    @Id
    private String id;

    /**
     * Nombre visible de la mesa (coincide con el ID: "1" a "10").
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Cantidad de personas que pueden sentarse en la mesa.
     * Mesas 1,2,9,10 = 2 personas | Mesas 3-8 = 4 personas.
     */
    @Column(nullable = false)
    private int capacidad = 4;

    /**
     * Estado actual de la mesa.
     * Por defecto: DISPONIBLE.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus estado = TableStatus.DISPONIBLE;

    /**
     * Constructor vacío requerido por JPA.
     */
    public DinningTable() {
    }

    /**
     * Constructor con ID manual, nombre y capacidad.
     * El estado queda DISPONIBLE por defecto.
     */
    public DinningTable(String id, String nombre, int capacidad) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.estado = TableStatus.DISPONIBLE;
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

    public TableStatus getEstado() {
        return estado;
    }

    public void setEstado(TableStatus estado) {
        this.estado = estado;
    }
}