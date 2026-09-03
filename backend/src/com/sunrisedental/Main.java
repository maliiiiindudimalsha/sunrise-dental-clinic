package com.sunrisedental;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;


import com.sunrisedental.controller.LoginController;
import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.controller.BillingController;
import com.sunrisedental.controller.DentistController;
import com.sunrisedental.controller.TreatmentController;
import com.sunrisedental.controller.ReportController;
import com.sunrisedental.controller.NotificationController;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/login", new LoginController());
        server.createContext("/appointments", new AppointmentController());
        server.createContext("/bills", new BillingController());
        server.createContext("/dentists", new DentistController());
        server.createContext("/treatments", new TreatmentController());
        server.createContext("/reports", new ReportController());
        server.createContext("/notifications", new NotificationController());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }
}