package com.sunrisedental;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sunrisedental.controller.*;
import com.sunrisedental.util.AuthFilter;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Public route
        server.createContext("/login", new LoginController());

        // Protected routes
        protect(server.createContext("/dashboard", new DashboardController()));
        protect(server.createContext("/appointments", new AppointmentController()));
        protect(server.createContext("/bills", new BillingController()));
        protect(server.createContext("/dentists", new DentistController()));
        protect(server.createContext("/treatments", new TreatmentController()));
        protect(server.createContext("/reports", new ReportController()));
        protect(server.createContext("/notifications", new NotificationController()));
        protect(server.createContext("/staff", new StaffController()));
        protect(server.createContext("/logout", new LogoutController()));

        server.start();

        System.out.println("Sunrise Dental Clinic Server started on port 8080");
    }

    private static void protect(HttpContext context) {
        context.getFilters().add(new AuthFilter());
    }
}