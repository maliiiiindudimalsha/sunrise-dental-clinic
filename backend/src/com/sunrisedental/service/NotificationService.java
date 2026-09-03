package com.sunrisedental.service;

import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {
    private NotificationDAO notificationDAO = new NotificationDAO();

    public void send(String appointmentNo, String channel, String message) throws SQLException {
        notificationDAO.insert(appointmentNo, channel, message);
    }

    public List<Notification> getAll() throws SQLException {
        return notificationDAO.findAll();
    }
}