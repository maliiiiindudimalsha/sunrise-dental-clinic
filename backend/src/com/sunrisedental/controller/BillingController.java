package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Bill;
import com.sunrisedental.service.BillingService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class BillingController implements HttpHandler {

    private final BillingService billingService =
            new BillingService();

    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        String method =
                exchange.getRequestMethod();

        String[] parts =
                exchange.getRequestURI()
                        .getPath()
                        .split("/");

        String appointmentNo =
                parts.length > 2 &&
                        !parts[2].isBlank()
                        ? parts[2]
                        : null;


        if (!"GET".equalsIgnoreCase(method)
                || appointmentNo == null) {

            sendResponse(
                    exchange,
                    405,
                    "{\"status\":\"error\",\"message\":\"Method not allowed\"}"
            );

            return;
        }


        try {

            Bill bill =
                    billingService
                            .getOrGenerateBill(
                                    appointmentNo
                            );


            if (bill == null) {

                sendResponse(
                        exchange,
                        404,
                        "{\"status\":\"error\",\"message\":\"Bill could not be generated\"}"
                );

                return;
            }


            sendResponse(
                    exchange,
                    200,
                    toJson(bill)
            );


        } catch (IllegalArgumentException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"status\":\"error\",\"message\":\""
                            + escapeJson(e.getMessage())
                            + "\"}"
            );


        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"status\":\"error\",\"message\":\""
                            + escapeJson(e.getMessage())
                            + "\"}"
            );
        }
    }


    private String toJson(Bill b) {

        return "{"
                + "\"appointmentNo\":\""
                + escapeJson(b.getAppointmentNo()) + "\","

                + "\"patientName\":\""
                + escapeJson(b.getPatientName()) + "\","

                + "\"dentistName\":\""
                + escapeJson(b.getDentistName()) + "\","

                + "\"treatmentName\":\""
                + escapeJson(b.getTreatmentName()) + "\","

                + "\"totalAmount\":"
                + b.getTotalAmount() + ","

                + "\"generatedDate\":\""
                + escapeJson(b.getGeneratedDate()) + "\""

                + "}";
    }


    private String escapeJson(String value) {

        if (value == null)
            return "";

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    private void sendResponse(
            HttpExchange exchange,
            int status,
            String response
    ) throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                status,
                bytes.length
        );

        try (OutputStream os =
                     exchange.getResponseBody()) {

            os.write(bytes);
        }
    }
}