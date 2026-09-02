package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.service.UserService;
import com.sunrisedental.model.User;

import java.io.IOException;
import java.io.OutputStream;

public class LoginController implements HttpHandler {
    private UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Allow CORS if needed
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if ("POST".equals(exchange.getRequestMethod())) {
            // Logic to handle login request payload can be expanded here
            String response = "{\"status\":\"success\",\"message\":\"Login endpoint reached\"}";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            String response = "{\"status\":\"error\",\"message\":\"Method not allowed\"}";
            exchange.sendResponseHeaders(405, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}