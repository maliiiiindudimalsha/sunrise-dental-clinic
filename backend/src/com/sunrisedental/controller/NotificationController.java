package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Notification;
import com.sunrisedental.service.NotificationService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationController implements HttpHandler {
    private NotificationService notificationService = new NotificationService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        String method = exchange.getRequestMethod();

        try {
            if ("GET".equals(method)) {
                sendResponse(exchange, 200, toJsonArray(notificationService.getAll()));
            } else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                String appointmentNo = extractJsonValue(body, "appointmentNo");
                String channel = extractJsonValue(body, "channel");
                String message = extractJsonValue(body, "message");
                notificationService.send(appointmentNo, channel, message);
                sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Notification sent\"}");
            } else {
                sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    private String toJsonArray(List<Notification> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Notification n = list.get(i);
            sb.append("{\"appointmentNo\":\"").append(nullSafe(n.getAppointmentNo()))
                    .append("\",\"channel\":\"").append(nullSafe(n.getChannel()))
                    .append("\",\"message\":\"").append(nullSafe(n.getMessage()).replace("\"", "'"))
                    .append("\",\"sentAt\":\"").append(nullSafe(n.getSentAt()))
                    .append("\"}");
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try (InputStream is = exchange.getRequestBody()) {
            while ((length = is.read(buffer)) != -1) result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    private String extractJsonValue(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^\",}]*)\"?").matcher(json);
        return m.find() ? m.group(1).trim() : null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}