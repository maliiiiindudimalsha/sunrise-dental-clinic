package com.sunrisedental;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import com.sunrisedental.controller.LoginController;
import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.controller.BillingController;
import com.sunrisedental.controller.DentistController;
import com.sunrisedental.controller.TreatmentController;
import com.sunrisedental.controller.ReportController;
import com.sunrisedental.controller.NotificationController;

import com.sunrisedental.util.AuthFilter;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {

        int port = 8080;

        HttpServer server = HttpServer.create(
                new InetSocketAddress(port),
                0
        );

        // --------------------------------
        // PUBLIC ENDPOINT
        // --------------------------------

        // Login does not require authentication
        server.createContext(
                "/login",
                new LoginController()
        );


        // --------------------------------
        // PROTECTED ENDPOINTS
        // --------------------------------

        // Appointments
        HttpContext appointmentContext =
                server.createContext(
                        "/appointments",
                        new AppointmentController()
                );

        appointmentContext.getFilters().add(
                new AuthFilter()
        );


        // Billing
        HttpContext billingContext =
                server.createContext(
                        "/bills",
                        new BillingController()
                );

        billingContext.getFilters().add(
                new AuthFilter()
        );


        // Dentists
        HttpContext dentistContext =
                server.createContext(
                        "/dentists",
                        new DentistController()
                );

        dentistContext.getFilters().add(
                new AuthFilter()
        );


        // Treatments
        HttpContext treatmentContext =
                server.createContext(
                        "/treatments",
                        new TreatmentController()
                );

        treatmentContext.getFilters().add(
                new AuthFilter()
        );


        // Reports
        HttpContext reportContext =
                server.createContext(
                        "/reports",
                        new ReportController()
                );

        reportContext.getFilters().add(
                new AuthFilter()
        );


        // Notifications
        HttpContext notificationContext =
                server.createContext(
                        "/notifications",
                        new NotificationController()
                );

        notificationContext.getFilters().add(
                new AuthFilter()
        );


        // --------------------------------
        // SERVER SETTINGS
        // --------------------------------

        server.setExecutor(null);

        server.start();

        System.out.println(
                "Sunrise Dental Clinic Server started on port " + port
        );

        System.out.println(
                "Protected API authentication enabled."
        );
    }
}