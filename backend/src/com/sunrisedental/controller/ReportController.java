package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.service.ReportService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ReportController implements HttpHandler {
    private ReportService reportService = new ReportService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

        try {
            String json = reportService.getReportJson(params.get("from"), params.get("to"), params.get("dentistId"));
            sendResponse(exchange, 200, json);
        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}