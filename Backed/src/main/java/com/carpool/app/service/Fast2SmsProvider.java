package com.carpool.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class Fast2SmsProvider implements SmsProvider {

    private final RestClient restClient;

    @Value("${sms.fast2sms.api-key:}")
    private String apiKey;

    @Value("${sms.fast2sms.otp-id:}")
    private String otpId;

    public Fast2SmsProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public void sendOtp(String phoneNumber, String code) {
        if (apiKey == null || apiKey.isBlank() || otpId == null || otpId.isBlank()) {
            throw new IllegalStateException("Fast2SMS API key and OTP template ID must be configured.");
        }

        JsonNode response = restClient.post()
                .uri("https://www.fast2sms.com/dev/otp/send")
                .header("Authorization", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "mobile", SmsPhoneNumber.toIndianDigits(phoneNumber),
                        "otp_id", otpId,
                        "otp_expiry", 5,
                        "otp_length", 6,
                        "otp", code
                ))
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.path("return").asBoolean(false)) {
            String reason = response == null ? "empty response" : response.path("message").asText("request rejected");
            throw new IllegalStateException("Fast2SMS did not accept the OTP request: " + reason);
        }
    }
}
