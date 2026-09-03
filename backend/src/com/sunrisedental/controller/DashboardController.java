package com.sunrisedental.controller;

import com.sun.net.httpserver.*;
import com.sunrisedental.service.DashboardService;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DashboardController implements HttpHandler {

    private final DashboardService service = new DashboardService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            return;
        }

        try {
            send(exchange, 200, service.getDashboard());
        } catch (Exception e) {
            e.printStackTrace();
            send(exchange, 500, "{\"status\":\"error\",\"message\":\"Unable to load dashboard\"}");
        }
    }

    private void send(HttpExchange e, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type", "application/json");
        e.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = e.getResponseBody()) {
            os.write(bytes);
        }
    }
}