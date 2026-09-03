package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.util.SessionManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LogoutController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            return;
        }

        String token = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        SessionManager.invalidate(token);

        sendResponse(exchange, 200,
                "{\"status\":\"success\",\"message\":\"Logged out successfully\"}");
    }

    private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}