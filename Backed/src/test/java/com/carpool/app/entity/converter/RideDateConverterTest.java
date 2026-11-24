package com.carpool.app.entity.converter;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RideDateConverterTest {

    private final RideDateConverter converter = new RideDateConverter();

    @Test
    void convertsBetweenLocalDateAndDayMonthTwoDigitYear() {
        LocalDate date = LocalDate.of(2026, 11, 24);

        assertEquals("24/11/26", converter.convertToDatabaseColumn(date));
        assertEquals(date, converter.convertToEntityAttribute("24/11/26"));
    }
}
