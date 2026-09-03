package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void insert(String appointmentNo, String channel, String message) throws SQLException {
        int appointmentId = getAppointmentId(appointmentNo);
        if (appointmentId == -1) {
            throw new SQLException("Appointment not found: " + appointmentNo);
        }

        String sql = "INSERT INTO notifications (appointment_id, message, channel) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.setString(2, message);
            ps.setString(3, channel);
            ps.executeUpdate();
        }
    }

    public List<Notification> findAll() throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.notification_id, a.appointment_no, n.channel, n.message, n.sent_at " +
                "FROM notifications n JOIN appointments a ON n.appointment_id = a.appointment_id " +
                "ORDER BY n.sent_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Notification n = new Notification();
                n.setNotificationId(rs.getInt("notification_id"));
                n.setAppointmentNo(rs.getString("appointment_no"));
                n.setChannel(rs.getString("channel"));
                n.setMessage(rs.getString("message"));
                n.setSentAt(String.valueOf(rs.getTimestamp("sent_at")));
                list.add(n);
            }
        }
        return list;
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
}