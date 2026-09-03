package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Bill;
import com.sunrisedental.service.BillingService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class BillingController implements HttpHandler {
    private BillingService billingService = new BillingService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath(); // /bills/APT00001
        String[] parts = path.split("/");
        String appointmentNo = parts.length > 2 ? parts[2] : null;

        if (!"GET".equals(method) || appointmentNo == null) {
            sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            return;
        }

        try {
            Bill bill = billingService.getOrGenerateBill(appointmentNo);
            sendResponse(exchange, 200, toJson(bill));
        } catch (SQLException e) {
            sendResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    private String toJson(Bill b) {
        return "{"
                + "\"appointmentNo\":\"" + nullSafe(b.getAppointmentNo()) + "\","
                + "\"patientName\":\"" + nullSafe(b.getPatientName()) + "\","
                + "\"dentistName\":\"" + nullSafe(b.getDentistName()) + "\","
                + "\"treatmentName\":\"" + nullSafe(b.getTreatmentName()) + "\","
                + "\"totalAmount\":" + b.getTotalAmount() + ","
                + "\"generatedDate\":\"" + nullSafe(b.getGeneratedDate()) + "\""
                + "}";
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}