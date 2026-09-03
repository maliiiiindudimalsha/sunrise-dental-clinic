package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.Appointment;

import java.sql.SQLException;

public class AppointmentService {
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    public Appointment register(Appointment appointment) throws SQLException {
        return appointmentDAO.register(appointment);
    }

    public Appointment search(String appointmentNo) throws SQLException {
        return appointmentDAO.findByAppointmentNo(appointmentNo);
    }

    public boolean update(String appointmentNo, Appointment appointment) throws SQLException {
        return appointmentDAO.update(appointmentNo, appointment);
    }

    public boolean cancel(String appointmentNo) throws SQLException {
        return appointmentDAO.cancel(appointmentNo);
    }
}