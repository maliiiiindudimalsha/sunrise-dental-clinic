package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Appointment;

import java.sql.*;

public class AppointmentDAO {

    public Appointment register(Appointment a) throws SQLException {
        String patientSql = "INSERT INTO patients (name, address, contact_number) VALUES (?, ?, ?)";
        String appointmentSql = "INSERT INTO appointments (patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            int patientId;
            int appointmentId;

            try {
                try (PreparedStatement ps1 = conn.prepareStatement(patientSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps1.setString(1, a.getPatientName());
                    ps1.setString(2, a.getAddress());
                    ps1.setString(3, a.getContactNumber());
                    ps1.executeUpdate();
                    try (ResultSet rs = ps1.getGeneratedKeys()) {
                        rs.next();
                        patientId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps2 = conn.prepareStatement(appointmentSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps2.setInt(1, patientId);
                    ps2.setInt(2, a.getDentistId());
                    ps2.setInt(3, a.getTreatmentId());
                    ps2.setString(4, a.getAppointmentDate());
                    ps2.setString(5, a.getAppointmentTime());
                    ps2.executeUpdate();
                    try (ResultSet rs = ps2.getGeneratedKeys()) {
                        rs.next();
                        appointmentId = rs.getInt(1);
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            // The AFTER INSERT trigger has already generated appointment_no by this point
            return findById(appointmentId);
        }
    }

    public Appointment findByAppointmentNo(String appointmentNo) throws SQLException {
        String sql = "SELECT a.*, p.name AS patient_name, p.address, p.contact_number, " +
                "d.name AS dentist_name, t.treatment_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_no = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Appointment findById(int appointmentId) throws SQLException {
        String sql = "SELECT a.*, p.name AS patient_name, p.address, p.contact_number, " +
                "d.name AS dentist_name, t.treatment_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean update(String appointmentNo, Appointment a) throws SQLException {
        String sql = "UPDATE appointments SET appointment_date = ?, appointment_time = ? WHERE appointment_no = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAppointmentDate());
            ps.setString(2, a.getAppointmentTime());
            ps.setString(3, appointmentNo);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancel(String appointmentNo) throws SQLException {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_no = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            return ps.executeUpdate() > 0;
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setAppointmentNo(rs.getString("appointment_no"));
        a.setPatientName(rs.getString("patient_name"));
        a.setAddress(rs.getString("address"));
        a.setContactNumber(rs.getString("contact_number"));
        a.setDentistName(rs.getString("dentist_name"));
        a.setTreatmentName(rs.getString("treatment_name"));
        a.setAppointmentDate(String.valueOf(rs.getDate("appointment_date")));
        a.setAppointmentTime(String.valueOf(rs.getTime("appointment_time")));
        a.setStatus(rs.getString("status"));
        return a;
    }
}