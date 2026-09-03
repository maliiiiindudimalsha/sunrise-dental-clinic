package com.sunrisedental.util;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthFilter extends Filter {

    @Override
    public String description() {
        return "Checks user authentication token";
    }

    @Override
    public void doFilter(
            HttpExchange exchange,
            Chain chain
    ) throws IOException {

        // --------------------------------
        // CORS HEADERS
        // --------------------------------

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type, X-Auth-Token"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );


        // --------------------------------
        // HANDLE OPTIONS REQUEST
        // --------------------------------

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod()
        )) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();

            return;
        }


        // --------------------------------
        // GET TOKEN
        // --------------------------------

        String token =
                exchange.getRequestHeaders()
                        .getFirst("X-Auth-Token");


        // --------------------------------
        // VALIDATE TOKEN
        // --------------------------------

        if (token != null
                && SessionManager.isValid(token)) {

            // Token valid
            chain.doFilter(exchange);

        } else {

            // Token invalid / missing
            sendUnauthorized(exchange);
        }
    }


    private void sendUnauthorized(
            HttpExchange exchange
    ) throws IOException {

        String response =
                "{"
                        + "\"status\":\"error\","
                        + "\"message\":\"Unauthorized. Please login first.\""
                        + "}";

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                401,
                bytes.length
        );

        try (
                OutputStream outputStream =
                        exchange.getResponseBody()
        ) {

            outputStream.write(bytes);
        }
    }
}