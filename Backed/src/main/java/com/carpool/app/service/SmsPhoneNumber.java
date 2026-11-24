package com.carpool.app.service;

final class SmsPhoneNumber {

    private SmsPhoneNumber() {}

    static String toIndianDigits(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number is required");
        }

        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith("91") && digits.length() == 12) {
            digits = digits.substring(2);
        } else if (digits.startsWith("0") && digits.length() == 11) {
            digits = digits.substring(1);
        }
        if (!digits.matches("[6-9]\\d{9}")) {
            throw new IllegalArgumentException("Enter a valid 10-digit Indian mobile number");
        }
        return digits;
    }

    static String toMsg91Format(String phoneNumber) {
        return "91" + toIndianDigits(phoneNumber);
    }

    static String toE164(String phoneNumber) {
        return "+" + toMsg91Format(phoneNumber);
    }
}
