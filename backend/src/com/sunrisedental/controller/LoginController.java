package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.User;
import com.sunrisedental.service.UserService;
import com.sunrisedental.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController implements HttpHandler {

    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type, X-Auth-Token"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "POST, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        // Browser preflight request
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        // Login only accepts POST
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(
                    exchange,
                    405,
                    "{\"status\":\"error\",\"message\":\"Method not allowed\"}"
            );
            return;
        }

        try {
            String body = readRequestBody(exchange);

            String username = extractJsonValue(body, "username");
            String password = extractJsonValue(body, "password");

            // Basic validation
            if (username == null || username.trim().isEmpty()
                    || password == null || password.isEmpty()) {

                sendResponse(
                        exchange,
                        400,
                        "{\"status\":\"error\",\"message\":\"Username and password are required\"}"
                );
                return;
            }

            User user = userService.login(
                    username.trim(),
                    password
            );

            if (user != null) {

                String token = SessionManager.createSession(
                        user.getUsername(),
                        user.getRole()
                );

                String response =
                        "{"
                                + "\"status\":\"success\","
                                + "\"message\":\"Login successful\","
                                + "\"token\":\"" + escapeJson(token) + "\","
                                + "\"role\":\"" + escapeJson(user.getRole()) + "\","
                                + "\"username\":\"" + escapeJson(user.getUsername()) + "\""
                                + "}";

                sendResponse(exchange, 200, response);

            } else {

                sendResponse(
                        exchange,
                        401,
                        "{\"status\":\"error\",\"message\":\"Invalid username or password\"}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"status\":\"error\",\"message\":\"Internal server error\"}"
            );
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        try (InputStream inputStream = exchange.getRequestBody()) {

            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
        }

        return result.toString(StandardCharsets.UTF_8);
    }

    private String extractJsonValue(String json, String key) {

        if (json == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"(.*?)\""
        );

        Matcher matcher = pattern.matcher(json);

        return matcher.find() ? matcher.group(1) : null;
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}