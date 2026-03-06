package com.redia.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una reserva dentro del sistema.
 * Cada instancia de esta clase corresponde a un registro
 * en la tabla "reservations" de la base de datos.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    /**
     * Identificador único de la reserva.
     * Se genera automáticamente usando UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Usuario que realizó la reserva.
     * Relación muchos a uno:
     * - Un usuario puede tener muchas reservas.
     * - Cada reserva pertenece a un solo usuario.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User cliente;

    /**
     * Fecha y hora para la cual se realiza la reserva.
     */
    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    /**
     * Número de personas incluidas en la reserva.
     */
    @Column(nullable = false)
    private int numeroPersonas;

    /**
     * Estado actual de la reserva dentro del sistema.
     *
     * @Enumerated(EnumType.STRING)
     * Hace que el enum se guarde como texto en la BD
     * (SOLICITADA, CONFIRMADA, etc.) en lugar de números.
     *
     * @Column(nullable = false)
     * Evita que se guarden reservas sin estado.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus estado;

    /**
     * Fecha en la que se creó la reserva.
     */
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Reservation() {
    }

    /**
     * Constructor utilizado para crear nuevas reservas.
     *
     * @param cliente usuario que realiza la reserva
     * @param fechaReserva fecha y hora reservada
     * @param numeroPersonas cantidad de personas
     * @param estado estado inicial de la reserva
     */
    public Reservation(User cliente, LocalDateTime fechaReserva, int numeroPersonas, ReservationStatus estado) {
        this.cliente = cliente;
        this.fechaReserva = fechaReserva;
        this.numeroPersonas = numeroPersonas;
        this.estado = estado;
        this.fechaCreacion = LocalDateTime.now();
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public String getId() {
        return id;
    }

    public User getCliente() {
        return cliente;
    }

    public void setCliente(User cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    public void setNumeroPersonas(int numeroPersonas) {
        this.numeroPersonas = numeroPersonas;
    }

    public ReservationStatus getEstado() {
        return estado;
    }

    public void setEstado(ReservationStatus estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}