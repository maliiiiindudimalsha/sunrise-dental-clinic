package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {


    public void insert(
            String appointmentNo,
            String channel,
            String message
    ) throws SQLException {

        String sql =
                "INSERT INTO notifications " +
                        "(appointment_id, message, channel) " +

                        "SELECT appointment_id, ?, ? " +
                        "FROM appointments " +

                        "WHERE appointment_no = ? " +
                        "AND status <> 'CANCELLED'";


        try (
                Connection conn =
                        DBConnection
                                .getInstance()
                                .getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {


            ps.setString(
                    1,
                    message
            );


            ps.setString(
                    2,
                    channel
            );


            ps.setString(
                    3,
                    appointmentNo
            );


            int inserted =
                    ps.executeUpdate();


            if (inserted == 0) {

                throw new IllegalArgumentException(
                        "Appointment not found or cancelled."
                );
            }
        }
    }


    public List<Notification> findAll()
            throws SQLException {

        List<Notification> list =
                new ArrayList<>();


        String sql =
                "SELECT " +
                        "n.notification_id, " +
                        "a.appointment_no, " +
                        "n.channel, " +
                        "n.message, " +
                        "n.sent_at " +

                        "FROM notifications n " +

                        "JOIN appointments a " +
                        "ON n.appointment_id = a.appointment_id " +

                        "ORDER BY n.sent_at DESC, " +
                        "n.notification_id DESC";


        try (
                Connection conn =
                        DBConnection
                                .getInstance()
                                .getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {


            while (rs.next()) {

                Notification n =
                        new Notification();


                n.setNotificationId(
                        rs.getInt(
                                "notification_id"
                        )
                );


                n.setAppointmentNo(
                        rs.getString(
                                "appointment_no"
                        )
                );


                n.setChannel(
                        rs.getString(
                                "channel"
                        )
                );


                n.setMessage(
                        rs.getString(
                                "message"
                        )
                );


                Timestamp sentAt =
                        rs.getTimestamp(
                                "sent_at"
                        );


                n.setSentAt(
                        sentAt == null
                                ? ""
                                : sentAt.toString()
                );


                list.add(n);
            }
        }


        return list;
    }
}