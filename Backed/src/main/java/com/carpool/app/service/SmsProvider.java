package com.carpool.app.service;

public interface SmsProvider {
    void sendOtp(String phoneNumber, String code);
}
