package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.service.TreatmentService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreatmentController implements HttpHandler {
    private TreatmentService treatmentService = new TreatmentService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath(); // /treatments  or  /treatments/2
        String[] parts = path.split("/");
        Integer id = (parts.length > 2 && !parts[2].isEmpty()) ? Integer.parseInt(parts[2]) : null;

        try {
            if ("GET".equals(method) && id == null) {
                sendResponse(exchange, 200, toJsonArray(treatmentService.getAll()));
            } else if ("POST".equals(method) && id == null) {
                handleAdd(exchange);
            } else if ("PUT".equals(method) && id != null) {
                handleUpdate(exchange, id);
            } else if ("DELETE".equals(method) && id != null) {
                boolean ok = treatmentService.delete(id);
                sendResponse(exchange, ok ? 200 : 404, ok
                        ? "{\"status\":\"success\"}" : "{\"status\":\"error\",\"message\":\"Not found\"}");
            } else {
                sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    private void handleAdd(HttpExchange exchange) throws IOException, SQLException {
        String body = readRequestBody(exchange);
        Treatment t = new Treatment();
        t.setTreatmentName(extractJsonValue(body, "treatmentName"));
        t.setConsultationFee(Double.parseDouble(extractJsonValue(body, "consultationFee")));
        Treatment saved = treatmentService.add(t);
        sendResponse(exchange, 200, toJson(saved));
    }

    private void handleUpdate(HttpExchange exchange, int id) throws IOException, SQLException {
        String body = readRequestBody(exchange);
        Treatment t = new Treatment();
        t.setTreatmentName(extractJsonValue(body, "treatmentName"));
        t.setConsultationFee(Double.parseDouble(extractJsonValue(body, "consultationFee")));
        boolean ok = treatmentService.update(id, t);
        sendResponse(exchange, ok ? 200 : 404, ok
                ? "{\"status\":\"success\"}" : "{\"status\":\"error\",\"message\":\"Not found\"}");
    }

    private String toJson(Treatment t) {
        return "{\"treatmentId\":" + t.getTreatmentId()
                + ",\"treatmentName\":\"" + nullSafe(t.getTreatmentName())
                + "\",\"consultationFee\":" + t.getConsultationFee() + "}";
    }

    private String toJsonArray(List<Treatment> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
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