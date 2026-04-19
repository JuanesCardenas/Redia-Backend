package com.redia.back.repository;

import com.redia.back.model.ChatbotSession;
import com.redia.back.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, String> {

    List<ChatbotSession> findByEstado(SessionStatus estado);

    long countByUsuarioIdAndFechaCreacionAfter(String usuarioId, LocalDateTime desde);
}
