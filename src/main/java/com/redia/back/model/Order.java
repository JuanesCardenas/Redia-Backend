package com.redia.back.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    // Id del pedido
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    // Reserva asociada
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // Fecha de creación del pedido
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    // Estado del pedido
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Platos del pedido
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDishes> dishes;

    // Total del pedido
    @Column(nullable = false)
    private Double total;

    // Pago asociado al pedido
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private OrderPayment payment;

    // Constructor vacío
    public Order() {
        this.fechaCreacion = LocalDateTime.now();
        this.status = OrderStatus.PENDIENTE;
    }

    // Constructor
    public Order(Reservation reservation, OrderStatus status, List<OrderDishes> dishes, Double total,
            OrderPayment payment) {
        this.reservation = reservation;
        this.status = status;
        this.dishes = dishes;
        this.total = total;
        this.payment = payment;
    }

    // Getters y Setters

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderDishes> getDishes() {
        return dishes;
    }

    public void setDishes(List<OrderDishes> dishes) {
        this.dishes = dishes;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public OrderPayment getPayment() {
        return payment;
    }

    public void setPayment(OrderPayment payment) {
        this.payment = payment;
    }
}