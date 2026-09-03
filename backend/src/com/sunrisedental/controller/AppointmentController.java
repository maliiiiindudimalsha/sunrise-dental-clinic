package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.AppointmentService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppointmentController implements HttpHandler {
    private AppointmentService appointmentService = new AppointmentService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath(); // /appointments  or  /appointments/APT00001
        String[] parts = path.split("/");
        String appointmentNo = parts.length > 2 ? parts[2] : null;

        try {
            if ("POST".equals(method) && appointmentNo == null) {
                handleRegister(exchange);
            } else if ("GET".equals(method) && appointmentNo != null) {
                handleSearch(exchange, appointmentNo);
            } else if ("PUT".equals(method) && appointmentNo != null) {
                handleUpdate(exchange, appointmentNo);
            } else if ("DELETE".equals(method) && appointmentNo != null) {
                handleCancel(exchange, appointmentNo);
            } else {
                sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Database error: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException, SQLException {
        String body = readRequestBody(exchange);

        Appointment a = new Appointment();
        a.setPatientName(extractJsonValue(body, "patientName"));
        a.setAddress(extractJsonValue(body, "address"));
        a.setContactNumber(extractJsonValue(body, "contactNumber"));
        a.setDentistId(Integer.parseInt(extractJsonValue(body, "dentistId")));
        a.setTreatmentId(Integer.parseInt(extractJsonValue(body, "treatmentId")));
        a.setAppointmentDate(extractJsonValue(body, "appointmentDate"));
        a.setAppointmentTime(extractJsonValue(body, "appointmentTime"));

        Appointment saved = appointmentService.register(a);
        sendResponse(exchange, 200, toJson(saved));
    }

    private void handleSearch(HttpExchange exchange, String appointmentNo) throws IOException, SQLException {
        Appointment a = appointmentService.search(appointmentNo);
        if (a == null) {
            sendResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Appointment not found\"}");
        } else {
            sendResponse(exchange, 200, toJson(a));
        }
    }

    private void handleUpdate(HttpExchange exchange, String appointmentNo) throws IOException, SQLException {
        String body = readRequestBody(exchange);
        Appointment a = new Appointment();
        a.setAppointmentDate(extractJsonValue(body, "appointmentDate"));
        a.setAppointmentTime(extractJsonValue(body, "appointmentTime"));

        boolean ok = appointmentService.update(appointmentNo, a);
        sendResponse(exchange, ok ? 200 : 404, ok
                ? "{\"status\":\"success\",\"message\":\"Appointment updated\"}"
                : "{\"status\":\"error\",\"message\":\"Appointment not found\"}");
    }

    private void handleCancel(HttpExchange exchange, String appointmentNo) throws IOException, SQLException {
        boolean ok = appointmentService.cancel(appointmentNo);
        sendResponse(exchange, ok ? 200 : 404, ok
                ? "{\"status\":\"success\",\"message\":\"Appointment cancelled\"}"
                : "{\"status\":\"error\",\"message\":\"Appointment not found\"}");
    }

    private String toJson(Appointment a) {
        return "{"
                + "\"appointmentNo\":\"" + nullSafe(a.getAppointmentNo()) + "\","
                + "\"patientName\":\"" + nullSafe(a.getPatientName()) + "\","
                + "\"address\":\"" + nullSafe(a.getAddress()) + "\","
                + "\"contactNumber\":\"" + nullSafe(a.getContactNumber()) + "\","
                + "\"dentistName\":\"" + nullSafe(a.getDentistName()) + "\","
                + "\"treatmentName\":\"" + nullSafe(a.getTreatmentName()) + "\","
                + "\"appointmentDate\":\"" + nullSafe(a.getAppointmentDate()) + "\","
                + "\"appointmentTime\":\"" + nullSafe(a.getAppointmentTime()) + "\","
                + "\"status\":\"" + nullSafe(a.getStatus()) + "\""
                + "}";
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