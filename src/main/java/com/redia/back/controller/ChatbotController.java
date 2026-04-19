package com.redia.back.controller;

import com.redia.back.dto.chatbot.ChatbotApiResponse;
import com.redia.back.dto.chatbot.CloseSessionRequestDTO;
import com.redia.back.dto.chatbot.EscalateRequestDTO;
import com.redia.back.dto.chatbot.FaqResponseDTO;
import com.redia.back.dto.chatbot.RateRequestDTO;
import com.redia.back.dto.chatbot.SessionResponseDTO;
import com.redia.back.service.ChatbotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/escalate")
    public ResponseEntity<ChatbotApiResponse<SessionResponseDTO>> escalate(
            @RequestBody EscalateRequestDTO request) {
        String userEmail = extractUserEmail();
        SessionResponseDTO data = chatbotService.escalateSession(request, userEmail);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ChatbotApiResponse<>(true, "Sesión escalada a agente", data));
    }

    @PostMapping("/rate")
    public ResponseEntity<ChatbotApiResponse<Void>> rate(
            @RequestBody RateRequestDTO request) {
        chatbotService.rateMessage(request);
        return ResponseEntity.ok(new ChatbotApiResponse<>(true, "Rating guardado correctamente"));
    }

    @PostMapping("/session/close")
    public ResponseEntity<ChatbotApiResponse<Void>> closeSession(
            @RequestBody CloseSessionRequestDTO request) {
        String userEmail = extractUserEmail();
        chatbotService.closeSession(request, userEmail);
        return ResponseEntity.ok(new ChatbotApiResponse<>(true, "Sesión cerrada"));
    }

    @GetMapping("/faqs")
    public ResponseEntity<ChatbotApiResponse<List<FaqResponseDTO>>> getFaqs() {
        List<FaqResponseDTO> faqs = chatbotService.getFaqs();
        return ResponseEntity.ok(new ChatbotApiResponse<>(true, null, faqs));
    }

    private String extractUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }
}
