package com.sunrisedental.service;

import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO =
            new NotificationDAO();


    public void send(
            String appointmentNo,
            String channel,
            String message
    ) throws SQLException {

        if (appointmentNo == null ||
                appointmentNo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment is required."
            );
        }


        if (channel == null ||
                (!channel.equalsIgnoreCase("SMS")
                        &&
                        !channel.equalsIgnoreCase("EMAIL"))) {

            throw new IllegalArgumentException(
                    "Invalid notification channel."
            );
        }


        if (message == null ||
                message.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Message is required."
            );
        }


        if (message.trim().length() > 500) {

            throw new IllegalArgumentException(
                    "Message is too long."
            );
        }


        notificationDAO.insert(
                appointmentNo.trim(),
                channel.toUpperCase(),
                message.trim()
        );
    }


    public List<Notification> getAll()
            throws SQLException {

        return notificationDAO.findAll();
    }
}