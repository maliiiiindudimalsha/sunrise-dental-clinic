package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Staff;
import com.sunrisedental.service.StaffService;
import com.sunrisedental.util.SessionManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaffController implements HttpHandler {
    private StaffService staffService = new StaffService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        String method = exchange.getRequestMethod();
        String[] parts = exchange.getRequestURI().getPath().split("/");
        // /staff  or  /staff/5  or  /staff/5/password  or  /staff/5/activate
        String token = exchange.getRequestHeaders().getFirst("X-Auth-Token");

        // The whole module is admin-only - no receptionist access at all.
        if (!"ADMIN".equalsIgnoreCase(SessionManager.getRole(token))) {
            sendResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Admin access required\"}");
            return;
        }

        try {
            Integer id = (parts.length > 2 && !parts[2].isBlank()) ? Integer.parseInt(parts[2]) : null;
            String action = parts.length > 3 ? parts[3] : null;

            if ("GET".equalsIgnoreCase(method) && id == null) {
                sendResponse(exchange, 200, toJsonArray(staffService.getAll()));

            } else if ("POST".equalsIgnoreCase(method) && id == null) {
                handleAdd(exchange);

            } else if ("PUT".equalsIgnoreCase(method) && id != null && "password".equals(action)) {
                handleResetPassword(exchange, id);

            } else if ("PUT".equalsIgnoreCase(method) && id != null && "activate".equals(action)) {
                boolean ok = staffService.setActive(id, true);
                sendResponse(exchange, ok ? 200 : 404, ok
                        ? "{\"status\":\"success\",\"message\":\"Staff account activated\"}"
                        : "{\"status\":\"error\",\"message\":\"Staff not found\"}");

            } else if ("PUT".equalsIgnoreCase(method) && id != null && action == null) {
                handleUpdateRole(exchange, id);

            } else if ("DELETE".equalsIgnoreCase(method) && id != null) {
                handleDeactivate(exchange, id, token);

            } else {
                sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            }

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Invalid staff ID\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAdd(HttpExchange exchange) throws IOException, SQLException {
        String body = readRequestBody(exchange);
        String username = extractJsonValue(body, "username");
        String password = extractJsonValue(body, "password");
        String role = extractJsonValue(body, "role");

        Staff saved = staffService.addStaff(username, password, role);
        sendResponse(exchange, 200, toJson(saved));
    }

    private void handleUpdateRole(HttpExchange exchange, int id) throws IOException, SQLException {
        String body = readRequestBody(exchange);
        String role = extractJsonValue(body, "role");

        boolean ok = staffService.updateRole(id, role);
        sendResponse(exchange, ok ? 200 : 404, ok
                ? "{\"status\":\"success\",\"message\":\"Role updated\"}"
                : "{\"status\":\"error\",\"message\":\"Staff not found\"}");
    }

    private void handleResetPassword(HttpExchange exchange, int id) throws IOException, SQLException {
        String body = readRequestBody(exchange);
        String password = extractJsonValue(body, "password");

        boolean ok = staffService.resetPassword(id, password);
        sendResponse(exchange, ok ? 200 : 404, ok
                ? "{\"status\":\"success\",\"message\":\"Password reset\"}"
                : "{\"status\":\"error\",\"message\":\"Staff not found\"}");
    }

    // Prevents an admin from deactivating the account they're currently logged in as.
    private void handleDeactivate(HttpExchange exchange, int id, String token) throws IOException, SQLException {
        String currentUsername = SessionManager.getUsername(token);
        String targetUsername = staffService.getUsernameById(id);

        if (targetUsername != null && targetUsername.equalsIgnoreCase(currentUsername)) {
            sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"You cannot deactivate your own account.\"}");
            return;
        }

        boolean ok = staffService.setActive(id, false);
        sendResponse(exchange, ok ? 200 : 404, ok
                ? "{\"status\":\"success\",\"message\":\"Staff account deactivated\"}"
                : "{\"status\":\"error\",\"message\":\"Staff not found\"}");
    }

    private String toJson(Staff s) {
        return "{"
                + "\"staffId\":" + s.getStaffId() + ","
                + "\"username\":\"" + escapeJson(s.getUsername()) + "\","
                + "\"role\":\"" + escapeJson(s.getRole()) + "\","
                + "\"active\":" + s.isActive()
                + "}";
    }

    private String toJsonArray(List<Staff> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

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
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?([^\",}]*)\"?").matcher(json);
        return m.find() ? m.group(1).trim() : null;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}