package com.redia.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_faqs")
public class ChatbotFaq {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, length = 500)
    private String pregunta;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String respuesta;

    /** Palabras clave separadas por coma */
    @Column(length = 500)
    private String keywords;

    /** IDs de preguntas relacionadas separados por coma */
    @Column(name = "related_questions", length = 500)
    private String preguntasRelacionadas;

    @Column(name = "created_at")
    private LocalDateTime fechaCreacion;

    @Column(name = "updated_at")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public ChatbotFaq() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getPreguntasRelacionadas() {
        return preguntasRelacionadas;
    }

    public void setPreguntasRelacionadas(String preguntasRelacionadas) {
        this.preguntasRelacionadas = preguntasRelacionadas;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
