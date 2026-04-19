package com.redia.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_ratings")
public class ChatbotRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "faq_id")
    private String faqId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingType calificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatbotSession sesion;

    @Column(name = "created_at")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public ChatbotRating() {
    }

    public Long getId() {
        return id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFaqId() {
        return faqId;
    }

    public void setFaqId(String faqId) {
        this.faqId = faqId;
    }

    public RatingType getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(RatingType calificacion) {
        this.calificacion = calificacion;
    }

    public ChatbotSession getSesion() {
        return sesion;
    }

    public void setSesion(ChatbotSession sesion) {
        this.sesion = sesion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
