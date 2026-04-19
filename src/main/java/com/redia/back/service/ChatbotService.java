package com.redia.back.service;

import com.redia.back.dto.chatbot.CloseSessionRequestDTO;
import com.redia.back.dto.chatbot.EscalateRequestDTO;
import com.redia.back.dto.chatbot.FaqResponseDTO;
import com.redia.back.dto.chatbot.RateRequestDTO;
import com.redia.back.dto.chatbot.SessionResponseDTO;

import java.util.List;

public interface ChatbotService {

    SessionResponseDTO escalateSession(EscalateRequestDTO request, String userEmail);

    void rateMessage(RateRequestDTO request);

    void closeSession(CloseSessionRequestDTO request, String userEmail);

    List<FaqResponseDTO> getFaqs();
}
