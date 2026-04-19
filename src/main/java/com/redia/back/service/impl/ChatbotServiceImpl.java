package com.redia.back.service.impl;

import com.redia.back.dto.chatbot.CloseSessionRequestDTO;
import com.redia.back.dto.chatbot.EscalateRequestDTO;
import com.redia.back.dto.chatbot.FaqResponseDTO;
import com.redia.back.dto.chatbot.MessageDTO;
import com.redia.back.dto.chatbot.RateRequestDTO;
import com.redia.back.dto.chatbot.SessionResponseDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.ChatbotFaq;
import com.redia.back.model.ChatbotMessage;
import com.redia.back.model.ChatbotRating;
import com.redia.back.model.ChatbotSession;
import com.redia.back.model.MessageType;
import com.redia.back.model.RatingType;
import com.redia.back.model.SessionStatus;
import com.redia.back.repository.ChatbotFaqRepository;
import com.redia.back.repository.ChatbotMessageRepository;
import com.redia.back.repository.ChatbotRatingRepository;
import com.redia.back.repository.ChatbotSessionRepository;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotServiceImpl.class);
    private static final int MAX_CONTENT_LENGTH = 5000;

    private final ChatbotSessionRepository sessionRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ChatbotRatingRepository ratingRepository;
    private final ChatbotFaqRepository faqRepository;
    private final UserRepository userRepository;

    public ChatbotServiceImpl(
            ChatbotSessionRepository sessionRepository,
            ChatbotMessageRepository messageRepository,
            ChatbotRatingRepository ratingRepository,
            ChatbotFaqRepository faqRepository,
            UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.ratingRepository = ratingRepository;
        this.faqRepository = faqRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public SessionResponseDTO escalateSession(EscalateRequestDTO request, String userEmail) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new BadRequestException("sessionId es requerido");
        }

        ChatbotSession session = sessionRepository.findById(request.getSessionId())
                .orElseGet(() -> createSession(request.getSessionId(), userEmail));

        session.setEstado(SessionStatus.ESPERANDO_AGENTE);
        session.setEscaladoAAgente(true);
        sessionRepository.save(session);

        saveMessages(session, request.getMessages());

        logger.info("Sesión {} escalada a agente", session.getId());

        SessionResponseDTO response = new SessionResponseDTO();
        response.setSessionId(session.getId());
        response.setStatus("waiting_for_agent");
        response.setAgentAssignedAt(null);
        return response;
    }

    @Override
    @Transactional
    public void rateMessage(RateRequestDTO request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new BadRequestException("sessionId es requerido");
        }
        if (request.getRating() == null
                || (!request.getRating().equalsIgnoreCase("positive")
                    && !request.getRating().equalsIgnoreCase("negative"))) {
            throw new BadRequestException("rating debe ser 'positive' o 'negative'");
        }

        ChatbotSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new BadRequestException("Sesión no encontrada: " + request.getSessionId()));

        RatingType ratingType = "positive".equalsIgnoreCase(request.getRating())
                ? RatingType.POSITIVE
                : RatingType.NEGATIVE;

        ChatbotRating rating = new ChatbotRating();
        rating.setMessageId(request.getMessageId());
        rating.setFaqId(request.getFaqId());
        rating.setCalificacion(ratingType);
        rating.setSesion(session);
        ratingRepository.save(rating);

        logger.info("Rating {} guardado para mensaje {} en sesión {}",
                ratingType, request.getMessageId(), request.getSessionId());
    }

    @Override
    @Transactional
    public void closeSession(CloseSessionRequestDTO request, String userEmail) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new BadRequestException("sessionId es requerido");
        }

        ChatbotSession session = sessionRepository.findById(request.getSessionId())
                .orElseGet(() -> createSession(request.getSessionId(), userEmail));

        session.setEstado(SessionStatus.CERRADA);
        session.setFechaCierre(LocalDateTime.now());
        sessionRepository.save(session);

        saveMessages(session, request.getMessages());

        logger.info("Sesión {} cerrada", session.getId());
    }

    @Override
    public List<FaqResponseDTO> getFaqs() {
        return faqRepository.findAll().stream()
                .map(this::toFaqDTO)
                .collect(Collectors.toList());
    }

    private ChatbotSession createSession(String sessionId, String userEmail) {
        ChatbotSession session = new ChatbotSession();
        session.setId(sessionId);
        if (userEmail != null) {
            userRepository.findByEmail(userEmail).ifPresent(session::setUsuario);
        }
        return session;
    }

    private void saveMessages(ChatbotSession session, List<MessageDTO> messages) {
        if (messages == null || messages.isEmpty()) return;

        for (MessageDTO msgDTO : messages) {
            if (msgDTO.getId() == null || msgDTO.getId().isBlank()) continue;
            if (messageRepository.existsById(msgDTO.getId())) continue;

            String content = msgDTO.getContent() != null ? msgDTO.getContent() : "";
            if (content.length() > MAX_CONTENT_LENGTH) {
                content = content.substring(0, MAX_CONTENT_LENGTH);
            }

            ChatbotMessage message = new ChatbotMessage();
            message.setId(msgDTO.getId());
            message.setSesion(session);
            message.setTipo(parseMessageType(msgDTO.getType()));
            message.setContenido(content);
            messageRepository.save(message);
        }
    }

    private MessageType parseMessageType(String type) {
        if (type == null) return MessageType.USER;
        return switch (type.toLowerCase()) {
            case "bot" -> MessageType.BOT;
            case "agent" -> MessageType.AGENT;
            default -> MessageType.USER;
        };
    }

    private FaqResponseDTO toFaqDTO(ChatbotFaq faq) {
        FaqResponseDTO dto = new FaqResponseDTO();
        dto.setId(faq.getId());
        dto.setCategory(faq.getCategoria());
        dto.setQuestion(faq.getPregunta());
        dto.setAnswer(faq.getRespuesta());
        dto.setKeywords(splitToList(faq.getKeywords()));
        dto.setRelatedQuestions(splitToList(faq.getPreguntasRelacionadas()));
        return dto;
    }

    private List<String> splitToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
