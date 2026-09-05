package com.sunrisedental.service;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppointmentServiceTest {

    @Test
    void shouldRejectPastTimeForToday() {
        AppointmentService service = new AppointmentService();

        String today = LocalDate.now().toString();

        String pastTime = LocalTime.now()
                .minusMinutes(30)
                .withSecond(0)
                .withNano(0)
                .toString();

        assertThrows(SQLException.class, () ->
                service.validateAppointmentDateTime(today, pastTime)
        );
    }
}