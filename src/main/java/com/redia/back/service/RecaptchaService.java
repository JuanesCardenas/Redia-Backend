package com.redia.back.service;

public interface RecaptchaService {
    boolean validateRecaptcha(String recaptchaToken);
}
