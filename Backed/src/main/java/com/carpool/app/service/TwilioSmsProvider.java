package com.carpool.app.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsProvider implements SmsProvider {

    @Value("${sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${sms.twilio.auth-token:}")
    private String authToken;

    @Value("${sms.twilio.from-number:}")
    private String fromNumber;

    @Override
    public void sendOtp(String phoneNumber, String code) {
        if (accountSid == null || accountSid.isBlank()
                || authToken == null || authToken.isBlank()
                || fromNumber == null || fromNumber.isBlank()) {
            throw new IllegalStateException("Twilio account SID, auth token, and from-number must be configured.");
        }

        Twilio.init(accountSid, authToken);
        Message.creator(
                new PhoneNumber(SmsPhoneNumber.toE164(phoneNumber)),
                new PhoneNumber(fromNumber),
                "Your Carpool OTP is " + code + ". Valid for 5 minutes."
        ).create();
    }
}
