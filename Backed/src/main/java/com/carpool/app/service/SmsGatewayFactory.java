package com.carpool.app.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;

@Configuration
public class SmsGatewayFactory {

    @Value("${sms.provider:MOCK}")
    private String smsProvider;

    @Primary
    @Bean
    public SmsProvider smsProviderBean(
            TwilioSmsProvider twilioSmsProvider,
            Msg91SmsProvider msg91SmsProvider,
            Fast2SmsProvider fast2SmsProvider
    ) {
        String provider = smsProvider.trim().toUpperCase();

        switch (provider) {
            case "TWILIO":
                return twilioSmsProvider;
            case "MSG91":
                return msg91SmsProvider;
            case "FAST2SMS":
                return fast2SmsProvider;
            case "MOCK":
                return new MockSmsProvider();
            default:
                throw new IllegalArgumentException("Unsupported SMS_PROVIDER: " + smsProvider);
        }
    }

    private static class MockSmsProvider implements SmsProvider {
        @Override
        public void sendOtp(String phoneNumber, String code) {
            System.out.println("[SMS MOCK] To: " + phoneNumber + " | Your Carpool OTP is " + code + ". Valid for 5 minutes.");
        }
    }
}
