package com.redia.back.dto.chatbot;

public class SessionResponseDTO {

    private String sessionId;
    private String status;
    private String agentAssignedAt; // ISO 8601 o null

    public SessionResponseDTO() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgentAssignedAt() {
        return agentAssignedAt;
    }

    public void setAgentAssignedAt(String agentAssignedAt) {
        this.agentAssignedAt = agentAssignedAt;
    }
}
