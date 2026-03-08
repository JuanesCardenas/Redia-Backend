package com.redia.back.service.impl;

import com.redia.back.service.RecaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RecaptchaServiceImpl implements RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.url}")
    private String recaptchaVerifyUrl;

    @Override
    public boolean validateRecaptcha(String recaptchaToken) {
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret);
        requestMap.add("response", recaptchaToken);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(recaptchaVerifyUrl, requestMap, Map.class);
            if (apiResponse != null) {
                Boolean success = (Boolean) apiResponse.get("success");
                return Boolean.TRUE.equals(success);
            }
        } catch (Exception e) {
            System.err.println("Error validando reCAPTCHA con Google: " + e.getMessage());
        }

        return false;
    }
}
