package com.redia.back.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "order_payments")
public class OrderPayment {

    // Id del pago
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    // Pedido asociado
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Método de pago
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod metodoPago;

    // Estado del pago
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // Monto pagado
    @Column(nullable = false)
    private Double monto;

    // Fecha del pago
    @Column(nullable = false)
    private LocalDateTime fechaPago;

    // Constructor vacío
    public OrderPayment() {
        this.fechaPago = LocalDateTime.now();
    }

    // Constructor
    public OrderPayment(Order order, PaymentMethod metodoPago, Double monto) {
        this.order = order;
        this.metodoPago = metodoPago;
        this.monto = monto;
        this.status = PaymentStatus.PENDIENTE;
        this.fechaPago = LocalDateTime.now();
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentMethod getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(PaymentMethod metodoPago) {
        this.metodoPago = metodoPago;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
