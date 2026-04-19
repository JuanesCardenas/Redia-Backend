package com.redia.back.repository;

import com.redia.back.model.ChatbotRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotRatingRepository extends JpaRepository<ChatbotRating, Long> {
}
