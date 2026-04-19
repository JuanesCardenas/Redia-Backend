package com.redia.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages")
public class ChatbotMessage {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatbotSession sesion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType tipo;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String contenido;

    @Column(name = "faq_id")
    private String faqId;

    @Column(name = "created_at")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public ChatbotMessage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChatbotSession getSesion() {
        return sesion;
    }

    public void setSesion(ChatbotSession sesion) {
        this.sesion = sesion;
    }

    public MessageType getTipo() {
        return tipo;
    }

    public void setTipo(MessageType tipo) {
        this.tipo = tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getFaqId() {
        return faqId;
    }

    public void setFaqId(String faqId) {
        this.faqId = faqId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
