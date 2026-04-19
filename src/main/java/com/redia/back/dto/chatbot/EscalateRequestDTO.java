package com.redia.back.dto.chatbot;

import java.util.List;

public class EscalateRequestDTO {

    private String sessionId;
    private List<MessageDTO> messages;

    public EscalateRequestDTO() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<MessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDTO> messages) {
        this.messages = messages;
    }
}
