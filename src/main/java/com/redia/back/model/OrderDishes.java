package com.redia.back.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_dishes")
public class OrderDishes {

    // Id del item del pedido
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Pedido al que pertenece
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Plato asociado
    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    // Cantidad del plato
    @Column(nullable = false)
    private int cantidad;

    // Precio del plato en el momento del pedido
    @Column(nullable = false)
    private Double precioUnitario;

    // Subtotal (precio * cantidad)
    @Column(nullable = false)
    private Double subtotal;

    // Constructor vacío
    public OrderDishes() {
    }

    // Constructor
    public OrderDishes(Order order, Dish dish, int cantidad, Double precioUnitario) {
        this.order = order;
        this.dish = dish;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
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

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
