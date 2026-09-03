package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Bill;

import java.sql.*;

public class BillDAO {

    public Bill findByAppointmentNo(String appointmentNo) throws SQLException {
        String sql = "SELECT b.bill_id, b.total_amount, b.generated_date, " +
                "a.appointment_no, p.name AS patient_name, d.name AS dentist_name, t.treatment_name " +
                "FROM bills b " +
                "JOIN appointments a ON b.appointment_id = a.appointment_id " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_no = ? " +
                "ORDER BY b.bill_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // Calls the sp_generate_bill stored procedure defined in schema.sql
    public void generateBill(String appointmentNo) throws SQLException {
        int appointmentId = getAppointmentId(appointmentNo);
        if (appointmentId == -1) {
            throw new SQLException("Appointment not found: " + appointmentNo);
        }

        try (Connection conn = DBConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_generate_bill(?)}")) {
            cs.setInt(1, appointmentId);
            cs.execute();
        }
    }

    private int getAppointmentId(String appointmentNo) throws SQLException {
        String sql = "SELECT appointment_id FROM appointments WHERE appointment_no = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setAppointmentNo(rs.getString("appointment_no"));
        b.setPatientName(rs.getString("patient_name"));
        b.setDentistName(rs.getString("dentist_name"));
        b.setTreatmentName(rs.getString("treatment_name"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setGeneratedDate(String.valueOf(rs.getTimestamp("generated_date")));
        return b;
    }
}