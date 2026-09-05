package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.Appointment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO =
            new AppointmentDAO();


    public Appointment register(
            Appointment appointment
    ) throws SQLException {

        validateAppointment(appointment);

        validateAppointmentDateTime(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        return appointmentDAO.register(
                appointment
        );
    }


    public List<Appointment> getAll()
            throws SQLException {

        return appointmentDAO.findAll();
    }


    public Appointment search(
            String appointmentNo
    ) throws SQLException {

        if (appointmentNo == null
                || appointmentNo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment number is required."
            );
        }

        return appointmentDAO.findByAppointmentNo(
                appointmentNo.trim()
        );
    }


    public boolean update(
            String appointmentNo,
            Appointment appointment
    ) throws SQLException {

        if (appointmentNo == null
                || appointmentNo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment number is required."
            );
        }

        validatePatientDetails(appointment);
        validateDate(appointment.getAppointmentDate());
        validateTime(appointment.getAppointmentTime());

        validateAppointmentDateTime(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        return appointmentDAO.update(
                appointmentNo.trim(),
                appointment
        );
    }


    public boolean cancel(
            String appointmentNo
    ) throws SQLException {

        if (appointmentNo == null
                || appointmentNo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment number is required."
            );
        }

        return appointmentDAO.cancel(
                appointmentNo.trim()
        );
    }


    private void validateAppointment(
            Appointment appointment
    ) {

        validatePatientDetails(appointment);
        validateDate(appointment.getAppointmentDate());
        validateTime(appointment.getAppointmentTime());

        if (appointment.getDentistId() <= 0) {

            throw new IllegalArgumentException(
                    "Please select a dentist."
            );
        }

        if (appointment.getTreatmentId() <= 0) {

            throw new IllegalArgumentException(
                    "Please select a treatment."
            );
        }
    }


    private void validatePatientDetails(
            Appointment appointment
    ) {

        if (appointment.getPatientName() == null
                || appointment.getPatientName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Patient name is required."
            );
        }


        if (appointment.getContactNumber() == null
                || appointment.getContactNumber().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Contact number is required."
            );
        }


        if (!appointment.getContactNumber()
                .matches("[0-9+\\- ]{7,20}")) {

            throw new IllegalArgumentException(
                    "Invalid contact number."
            );
        }


        if (appointment.getAddress() == null
                || appointment.getAddress().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Address is required."
            );
        }
    }


    private void validateDate(String dateString) {

        if (dateString == null
                || dateString.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment date is required."
            );
        }

        try {

            LocalDate date =
                    LocalDate.parse(dateString);

            if (date.isBefore(LocalDate.now())) {

                throw new IllegalArgumentException(
                        "Appointment date cannot be in the past."
                );
            }

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid date format."
            );
        }
    }


    private void validateTime(String timeString) {

        if (timeString == null
                || timeString.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment time is required."
            );
        }

        try {

            LocalTime.parse(timeString);

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid appointment time."
            );
        }
    }


    void validateAppointmentDateTime(
            String dateString,
            String timeString
    ) throws SQLException {

        LocalDate date =
                parseAppointmentDate(dateString);

        LocalTime time =
                parseAppointmentTime(timeString);

        if (isPastAppointmentTime(date, time)) {

            throw new SQLException(
                    "Appointment time cannot be in the past."
            );
        }
    }


    private LocalDate parseAppointmentDate(
            String dateString
    ) throws SQLException {

        try {

            return LocalDate.parse(dateString);

        } catch (DateTimeParseException e) {

            throw new SQLException(
                    "Invalid appointment date or time format."
            );
        }
    }


    private LocalTime parseAppointmentTime(
            String timeString
    ) throws SQLException {

        try {

            return LocalTime.parse(timeString);

        } catch (DateTimeParseException e) {

            throw new SQLException(
                    "Invalid appointment date or time format."
            );
        }
    }


    private boolean isPastAppointmentTime(
            LocalDate date,
            LocalTime time
    ) {

        return date.isEqual(LocalDate.now())
                && time.isBefore(LocalTime.now());
    }
}