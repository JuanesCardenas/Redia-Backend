package com.redia.back.dto;

public record EmailDTO(String subject,
                       String body,
                       String recipient) {
}