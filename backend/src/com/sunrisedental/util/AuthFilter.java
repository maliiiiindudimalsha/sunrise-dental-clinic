package com.sunrisedental.util;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// Attach this to any HttpContext that should require a logged-in session.
// It also answers CORS preflight (OPTIONS) requests directly, which the
// browser will send automatically once we start sending a custom
// X-Auth-Token header from the frontend.
public class AuthFilter extends Filter {

    @Override
    public String description() {
        return "Requires a valid X-Auth-Token session before allowing access";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, X-Auth-Token");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String token = exchange.getRequestHeaders().getFirst("X-Auth-Token");

        if (SessionManager.isValid(token)) {
            chain.doFilter(exchange);
        } else {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            String response = "{\"status\":\"error\",\"message\":\"Unauthorized - please log in\"}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(401, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}