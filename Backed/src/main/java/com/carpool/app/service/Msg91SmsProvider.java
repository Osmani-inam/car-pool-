package com.carpool.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class Msg91SmsProvider implements SmsProvider {

    private final RestClient restClient;

    @Value("${sms.msg91.auth-key:}")
    private String authKey;

    @Value("${sms.msg91.template-id:}")
    private String templateId;

    public Msg91SmsProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public void sendOtp(String phoneNumber, String code) {
        if (authKey == null || authKey.isBlank() || templateId == null || templateId.isBlank()) {
            throw new IllegalStateException("MSG91 auth key and OTP template ID must be configured.");
        }

        JsonNode response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("control.msg91.com")
                        .path("/api/v5/otp")
                        .queryParam("template_id", templateId)
                        .queryParam("mobile", SmsPhoneNumber.toMsg91Format(phoneNumber))
                        .queryParam("otp", code)
                        .queryParam("otp_expiry", 5)
                        .build())
                .header("authkey", authKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of())
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !"success".equalsIgnoreCase(response.path("type").asText())) {
            String reason = response == null ? "empty response" : response.path("message").asText("request rejected");
            throw new IllegalStateException("MSG91 did not accept the OTP request: " + reason);
        }
    }
}
