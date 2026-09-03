package com.sunrisedental;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import com.sunrisedental.controller.*;
import com.sunrisedental.util.AuthFilter;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Public
        server.createContext("/login", new LoginController());

        // Protected
        protect(server.createContext("/appointments", new AppointmentController()));
        protect(server.createContext("/bills", new BillingController()));
        protect(server.createContext("/dentists", new DentistController()));
        protect(server.createContext("/treatments", new TreatmentController()));
        protect(server.createContext("/reports", new ReportController()));
        protect(server.createContext("/notifications", new NotificationController()));
        protect(server.createContext("/logout", new LogoutController()));

        server.setExecutor(null);
        server.start();

        System.out.println("Sunrise Dental Clinic Server started on port " + port);
    }

    private static void protect(HttpContext context) {
        context.getFilters().add(new AuthFilter());
    }
}