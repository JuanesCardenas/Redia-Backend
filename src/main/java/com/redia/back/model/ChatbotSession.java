package com.redia.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_sessions")
public class ChatbotSession {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus estado;

    @Column(name = "created_at")
    private LocalDateTime fechaCreacion;

    @Column(name = "closed_at")
    private LocalDateTime fechaCierre;

    @Column(name = "escalated_to_agent")
    private boolean escaladoAAgente = false;

    @Column(name = "agent_assigned_at")
    private LocalDateTime fechaAsignacionAgente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agente;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = SessionStatus.ABIERTA;
        }
    }

    public ChatbotSession() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public SessionStatus getEstado() {
        return estado;
    }

    public void setEstado(SessionStatus estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public boolean isEscaladoAAgente() {
        return escaladoAAgente;
    }

    public void setEscaladoAAgente(boolean escaladoAAgente) {
        this.escaladoAAgente = escaladoAAgente;
    }

    public LocalDateTime getFechaAsignacionAgente() {
        return fechaAsignacionAgente;
    }

    public void setFechaAsignacionAgente(LocalDateTime fechaAsignacionAgente) {
        this.fechaAsignacionAgente = fechaAsignacionAgente;
    }

    public User getAgente() {
        return agente;
    }

    public void setAgente(User agente) {
        this.agente = agente;
    }
}
