package com.sunrisedental.model;

public class Notification {

    private int notificationId;
    private String appointmentNo;
    private String channel;
    private String message;
    private String sentAt;


    public Notification() {}


    public int getNotificationId() {
        return notificationId;
    }


    public void setNotificationId(
            int notificationId
    ) {
        this.notificationId =
                notificationId;
    }


    public String getAppointmentNo() {
        return appointmentNo;
    }


    public void setAppointmentNo(
            String appointmentNo
    ) {
        this.appointmentNo =
                appointmentNo;
    }


    public String getChannel() {
        return channel;
    }


    public void setChannel(
            String channel
    ) {
        this.channel =
                channel;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(
            String message
    ) {
        this.message =
                message;
    }


    public String getSentAt() {
        return sentAt;
    }


    public void setSentAt(
            String sentAt
    ) {
        this.sentAt =
                sentAt;
    }
}