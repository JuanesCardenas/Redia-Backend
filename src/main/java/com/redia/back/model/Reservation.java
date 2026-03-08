package com.redia.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
     * Fecha y hora de inicio de la reserva.
     */
    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    /**
     * Fecha y hora en la que termina la reserva.
     */
    @Column(nullable = false)
    private LocalDateTime horaFinReserva;

    /**
     * Número de personas incluidas en la reserva.
     */
    @Column(nullable = false)
    private int numeroPersonas;

    /**
     * Número de mesas asociadas a la reserva.
     * Este valor debe coincidir con el tamaño de la lista de mesas.
     */
    @Column(nullable = false)
    @Min(1)
    private int numeroMesas;

    /**
     * Lista de mesas asociadas a la reserva.
     * 
     * Relación muchos a muchos:
     * - Una reserva puede tener varias mesas.
     * - Una mesa puede participar en distintas reservas en diferentes horarios.
     */
    @ManyToMany
    @JoinTable(name = "reservation_tables", joinColumns = @JoinColumn(name = "reservation_id"), inverseJoinColumns = @JoinColumn(name = "table_id"))
    private List<DinningTable> mesas = new ArrayList<>();

    /**
     * Estado actual de la reserva dentro del sistema.
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
     */
    public Reservation(User cliente,
            LocalDateTime fechaReserva,
            LocalDateTime horaFinReserva,
            int numeroPersonas,
            List<DinningTable> mesas,
            ReservationStatus estado) {

        this.cliente = cliente;
        this.fechaReserva = fechaReserva;
        this.horaFinReserva = horaFinReserva;
        this.numeroPersonas = numeroPersonas;
        this.mesas = mesas;
        this.numeroMesas = mesas.size();
        this.estado = estado;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Constructor simplificado con estado por defecto.
     */
    public Reservation(User cliente,
            LocalDateTime fechaReserva,
            LocalDateTime horaFinReserva,
            int numeroPersonas,
            List<DinningTable> mesas) {

        this.cliente = cliente;
        this.fechaReserva = fechaReserva;
        this.horaFinReserva = horaFinReserva;
        this.numeroPersonas = numeroPersonas;
        this.mesas = mesas;
        this.numeroMesas = mesas.size();
        this.estado = ReservationStatus.SOLICITADA;
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

    public LocalDateTime getHoraFinReserva() {
        return horaFinReserva;
    }

    public void setHoraFinReserva(LocalDateTime horaFinReserva) {
        this.horaFinReserva = horaFinReserva;
    }

    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    public void setNumeroPersonas(int numeroPersonas) {
        this.numeroPersonas = numeroPersonas;
    }

    public int getNumeroMesas() {
        return numeroMesas;
    }

    public void setNumeroMesas(int numeroMesas) {
        this.numeroMesas = numeroMesas;
    }

    public List<DinningTable> getMesas() {
        return mesas;
    }

    public void setMesas(List<DinningTable> mesas) {
        this.mesas = mesas;
        this.numeroMesas = mesas.size();
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