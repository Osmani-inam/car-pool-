package com.carpool.app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmsPhoneNumberTest {

    @Test
    void formatsIndianPhoneNumbersForSmsProviders() {
        assertEquals("9876543210", SmsPhoneNumber.toIndianDigits("09876543210"));
        assertEquals("919876543210", SmsPhoneNumber.toMsg91Format("+91 98765 43210"));
        assertEquals("+919876543210", SmsPhoneNumber.toE164("9876543210"));
    }

    @Test
    void rejectsInvalidIndianPhoneNumbers() {
        assertThrows(IllegalArgumentException.class, () -> SmsPhoneNumber.toIndianDigits("12345"));
    }
}
