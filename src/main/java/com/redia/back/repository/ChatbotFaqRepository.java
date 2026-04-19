package com.redia.back.repository;

import com.redia.back.model.ChatbotFaq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotFaqRepository extends JpaRepository<ChatbotFaq, String> {
}
