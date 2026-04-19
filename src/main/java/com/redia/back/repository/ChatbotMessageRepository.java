package com.redia.back.repository;

import com.redia.back.model.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, String> {

    List<ChatbotMessage> findBySesionId(String sesionId);

    long countBySesionId(String sesionId);
}
