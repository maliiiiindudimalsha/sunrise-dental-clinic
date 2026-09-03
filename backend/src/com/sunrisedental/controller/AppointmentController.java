package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.AppointmentService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppointmentController implements HttpHandler {

    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {


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

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String[] parts = path.split("/");
        String appointmentNo =
                parts.length > 2 && !parts[2].isBlank()
                        ? parts[2]
                        : null;

        try {

            if ("POST".equalsIgnoreCase(method) && appointmentNo == null) {

                handleRegister(exchange);

            } else if ("GET".equalsIgnoreCase(method) && appointmentNo != null) {

                handleSearch(exchange, appointmentNo);

            } else if ("PUT".equalsIgnoreCase(method) && appointmentNo != null) {

                handleUpdate(exchange, appointmentNo);

            } else if ("DELETE".equalsIgnoreCase(method) && appointmentNo != null) {

                handleCancel(exchange, appointmentNo);

            } else {

                sendResponse(
                        exchange,
                        405,
                        "{\"status\":\"error\",\"message\":\"Method not allowed\"}"
                );
            }

        } catch (IllegalArgumentException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"status\":\"error\",\"message\":\""
                            + escapeJson(e.getMessage()) + "\"}"
            );

        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"status\":\"error\",\"message\":\""
                            + escapeJson(e.getMessage()) + "\"}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"status\":\"error\",\"message\":\"Internal server error\"}"
            );
        }
    }

    private void handleRegister(HttpExchange exchange)
            throws IOException, SQLException {

        String body = readRequestBody(exchange);

        Appointment appointment = new Appointment();

        appointment.setPatientName(
                extractJsonValue(body, "patientName")
        );

        appointment.setAddress(
                extractJsonValue(body, "address")
        );

        appointment.setContactNumber(
                extractJsonValue(body, "contactNumber")
        );

        String dentistId =
                extractJsonValue(body, "dentistId");

        String treatmentId =
                extractJsonValue(body, "treatmentId");

        if (dentistId == null || treatmentId == null) {
            throw new IllegalArgumentException(
                    "Dentist and treatment are required."
            );
        }

        appointment.setDentistId(
                Integer.parseInt(dentistId)
        );

        appointment.setTreatmentId(
                Integer.parseInt(treatmentId)
        );

        appointment.setAppointmentDate(
                extractJsonValue(body, "appointmentDate")
        );

        appointment.setAppointmentTime(
                extractJsonValue(body, "appointmentTime")
        );

        Appointment saved =
                appointmentService.register(appointment);

        sendResponse(
                exchange,
                200,
                toJson(saved)
        );
    }

    private void handleSearch(
            HttpExchange exchange,
            String appointmentNo
    ) throws IOException, SQLException {

        Appointment appointment =
                appointmentService.search(appointmentNo);

        if (appointment == null) {

            sendResponse(
                    exchange,
                    404,
                    "{\"status\":\"error\",\"message\":\"Appointment not found\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    200,
                    toJson(appointment)
            );
        }
    }

    private void handleUpdate(
            HttpExchange exchange,
            String appointmentNo
    ) throws IOException, SQLException {

        String body = readRequestBody(exchange);

        Appointment appointment = new Appointment();

        appointment.setPatientName(
                extractJsonValue(body, "patientName")
        );

        appointment.setContactNumber(
                extractJsonValue(body, "contactNumber")
        );

        appointment.setAddress(
                extractJsonValue(body, "address")
        );

        appointment.setAppointmentDate(
                extractJsonValue(body, "appointmentDate")
        );

        appointment.setAppointmentTime(
                extractJsonValue(body, "appointmentTime")
        );

        boolean updated =
                appointmentService.update(
                        appointmentNo,
                        appointment
                );

        if (updated) {

            sendResponse(
                    exchange,
                    200,
                    "{\"status\":\"success\",\"message\":\"Appointment updated successfully\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    404,
                    "{\"status\":\"error\",\"message\":\"Appointment not found\"}"
            );
        }
    }

    private void handleCancel(
            HttpExchange exchange,
            String appointmentNo
    ) throws IOException, SQLException {

        boolean cancelled =
                appointmentService.cancel(appointmentNo);

        if (cancelled) {

            sendResponse(
                    exchange,
                    200,
                    "{\"status\":\"success\",\"message\":\"Appointment cancelled successfully\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    404,
                    "{\"status\":\"error\",\"message\":\"Appointment not found\"}"
            );
        }
    }

    private String toJson(Appointment a) {

        return "{"
                + "\"appointmentNo\":\"" + escapeJson(a.getAppointmentNo()) + "\","
                + "\"patientName\":\"" + escapeJson(a.getPatientName()) + "\","
                + "\"address\":\"" + escapeJson(a.getAddress()) + "\","
                + "\"contactNumber\":\"" + escapeJson(a.getContactNumber()) + "\","
                + "\"dentistName\":\"" + escapeJson(a.getDentistName()) + "\","
                + "\"treatmentName\":\"" + escapeJson(a.getTreatmentName()) + "\","
                + "\"appointmentDate\":\"" + escapeJson(a.getAppointmentDate()) + "\","
                + "\"appointmentTime\":\"" + escapeJson(a.getAppointmentTime()) + "\","
                + "\"status\":\"" + escapeJson(a.getStatus()) + "\""
                + "}";
    }

    private String readRequestBody(
            HttpExchange exchange
    ) throws IOException {

        ByteArrayOutputStream result =
                new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int length;

        try (InputStream inputStream =
                     exchange.getRequestBody()) {

            while ((length =
                    inputStream.read(buffer)) != -1) {

                result.write(
                        buffer,
                        0,
                        length
                );
            }
        }

        return result.toString(
                StandardCharsets.UTF_8
        );
    }

    private String extractJsonValue(
            String json,
            String key
    ) {

        if (json == null) {
            return null;
        }

        Matcher matcher =
                Pattern.compile(
                        "\"" + Pattern.quote(key)
                                + "\"\\s*:\\s*\"?([^\",}]*)\"?"
                ).matcher(json);

        return matcher.find()
                ? matcher.group(1).trim()
                : null;
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(bytes);
        }
    }
}