package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.Appointment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AppointmentService {
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    public Appointment register(Appointment appointment) throws SQLException {
        validateNotInPast(appointment.getAppointmentDate(), "register an appointment for");
        return appointmentDAO.register(appointment);
    }

    public Appointment search(String appointmentNo) throws SQLException {
        return appointmentDAO.findByAppointmentNo(appointmentNo);
    }

    public boolean update(String appointmentNo, Appointment appointment) throws SQLException {
        validateNotInPast(appointment.getAppointmentDate(), "reschedule an appointment to");
        return appointmentDAO.update(appointmentNo, appointment);
    }

    public boolean cancel(String appointmentNo) throws SQLException {
        return appointmentDAO.cancel(appointmentNo);
    }

    // Rejects any date before today. Reuses SQLException so the existing
    // controller/error-handling path (which already catches SQLException
    // and returns it as the response message) handles this with no
    // extra plumbing needed.
    private void validateNotInPast(String dateStr, String actionDescription) throws SQLException {
        try {
            LocalDate requestedDate = LocalDate.parse(dateStr);
            if (requestedDate.isBefore(LocalDate.now())) {
                throw new SQLException("Cannot " + actionDescription + " a past date.");
            }
        } catch (DateTimeParseException e) {
            throw new SQLException("Invalid date format. Expected YYYY-MM-DD.");
        }
    }
}