package com.redia.back.service.impl;

import com.redia.back.service.RecaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * Servicio para validar tokens de reCAPTCHA v3 con Google.
 * Optimizado con timeouts para evitar bloqueos prolongados.
 */
@Service
public class RecaptchaServiceImpl implements RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.url}")
    private String recaptchaVerifyUrl;

    private final RestTemplate restTemplate;

    public RecaptchaServiceImpl(RestTemplateBuilder builder) {
        // Configurar timeouts para evitar que se bloquee indefinidamente
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public boolean validateRecaptcha(String recaptchaToken) {
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            System.err.println("reCAPTCHA token es null o vacío");
            return false;
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret);
        requestMap.add("response", recaptchaToken);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(
                    recaptchaVerifyUrl,
                    requestMap,
                    Map.class
            );

            if (apiResponse != null) {
                Boolean success = (Boolean) apiResponse.get("success");
                Double score = (Double) apiResponse.get("score");

                // reCAPTCHA v3 devuelve un score entre 0 y 1
                // 0 = probablemente spam, 1 = probablemente legítimo
                // Aceptar score >= 0.5 es generalmente bueno
                boolean isValid = Boolean.TRUE.equals(success) && (score == null || score >= 0.5);

                if (!isValid) {
                    System.err.println("reCAPTCHA validación fallida. Success: " + success + ", Score: " + score);
                }

                return isValid;
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            System.err.println("Timeout o error de conexión validando reCAPTCHA: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error validando reCAPTCHA con Google: " + e.getMessage());
        }

        return false;
    }
}
