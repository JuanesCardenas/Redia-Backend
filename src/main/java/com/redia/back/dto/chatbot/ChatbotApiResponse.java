package com.redia.back.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatbotApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ChatbotApiResponse() {
    }

    public ChatbotApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ChatbotApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
