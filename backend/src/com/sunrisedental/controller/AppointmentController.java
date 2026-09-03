package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.AppointmentService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppointmentController
        implements HttpHandler {

    private final AppointmentService appointmentService =
            new AppointmentService();


    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        String method =
                exchange.getRequestMethod();

        String path =
                exchange.getRequestURI().getPath();

        String[] parts =
                path.split("/");

        String appointmentNo =
                parts.length > 2 && !parts[2].isBlank()
                        ? parts[2]
                        : null;


        try {

            // GET ALL APPOINTMENTS
            if ("GET".equalsIgnoreCase(method)
                    && appointmentNo == null) {

                handleGetAll(exchange);
            }

            // REGISTER
            else if ("POST".equalsIgnoreCase(method)
                    && appointmentNo == null) {

                handleRegister(exchange);
            }

            // SEARCH ONE
            else if ("GET".equalsIgnoreCase(method)
                    && appointmentNo != null) {

                handleSearch(
                        exchange,
                        appointmentNo
                );
            }

            // UPDATE
            else if ("PUT".equalsIgnoreCase(method)
                    && appointmentNo != null) {

                handleUpdate(
                        exchange,
                        appointmentNo
                );
            }

            // CANCEL
            else if ("DELETE".equalsIgnoreCase(method)
                    && appointmentNo != null) {

                handleCancel(
                        exchange,
                        appointmentNo
                );
            }

            else {

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
                            + escapeJson(e.getMessage())
                            + "\"}"
            );

        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"status\":\"error\",\"message\":\""
                            + escapeJson(e.getMessage())
                            + "\"}"
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


    // NEW
    private void handleGetAll(
            HttpExchange exchange
    ) throws IOException, SQLException {

        List<Appointment> appointments =
                appointmentService.getAll();

        sendResponse(
                exchange,
                200,
                toJsonArray(appointments)
        );
    }


    private void handleRegister(
            HttpExchange exchange
    ) throws IOException, SQLException {

        String body =
                readRequestBody(exchange);

        Appointment appointment =
                new Appointment();


        appointment.setPatientName(
                extractJsonValue(
                        body,
                        "patientName"
                )
        );


        appointment.setAddress(
                extractJsonValue(
                        body,
                        "address"
                )
        );


        appointment.setContactNumber(
                extractJsonValue(
                        body,
                        "contactNumber"
                )
        );


        String dentistId =
                extractJsonValue(
                        body,
                        "dentistId"
                );


        String treatmentId =
                extractJsonValue(
                        body,
                        "treatmentId"
                );


        if (dentistId == null
                || treatmentId == null) {

            throw new IllegalArgumentException(
                    "Dentist and treatment are required."
            );
        }


        appointment.setDentistId(
                Integer.parseInt(
                        dentistId
                )
        );


        appointment.setTreatmentId(
                Integer.parseInt(
                        treatmentId
                )
        );


        appointment.setAppointmentDate(
                extractJsonValue(
                        body,
                        "appointmentDate"
                )
        );


        appointment.setAppointmentTime(
                extractJsonValue(
                        body,
                        "appointmentTime"
                )
        );


        Appointment saved =
                appointmentService.register(
                        appointment
                );


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
                appointmentService.search(
                        appointmentNo
                );


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

        String body =
                readRequestBody(exchange);

        Appointment appointment =
                new Appointment();


        appointment.setPatientName(
                extractJsonValue(
                        body,
                        "patientName"
                )
        );


        appointment.setContactNumber(
                extractJsonValue(
                        body,
                        "contactNumber"
                )
        );


        appointment.setAddress(
                extractJsonValue(
                        body,
                        "address"
                )
        );


        appointment.setAppointmentDate(
                extractJsonValue(
                        body,
                        "appointmentDate"
                )
        );


        appointment.setAppointmentTime(
                extractJsonValue(
                        body,
                        "appointmentTime"
                )
        );


        boolean updated =
                appointmentService.update(
                        appointmentNo,
                        appointment
                );


        sendResponse(
                exchange,
                updated ? 200 : 404,
                updated
                        ? "{\"status\":\"success\",\"message\":\"Appointment updated successfully\"}"
                        : "{\"status\":\"error\",\"message\":\"Appointment not found\"}"
        );
    }


    private void handleCancel(
            HttpExchange exchange,
            String appointmentNo
    ) throws IOException, SQLException {

        boolean cancelled =
                appointmentService.cancel(
                        appointmentNo
                );


        sendResponse(
                exchange,
                cancelled ? 200 : 404,
                cancelled
                        ? "{\"status\":\"success\",\"message\":\"Appointment cancelled successfully\"}"
                        : "{\"status\":\"error\",\"message\":\"Appointment not found or already cancelled\"}"
        );
    }


    private String toJsonArray(
            List<Appointment> appointments
    ) {

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < appointments.size();
             i++) {

            json.append(
                    toJson(
                            appointments.get(i)
                    )
            );

            if (i < appointments.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        return json.toString();
    }


    private String toJson(Appointment a) {

        return "{"
                + "\"appointmentNo\":\""
                + escapeJson(a.getAppointmentNo()) + "\","

                + "\"patientName\":\""
                + escapeJson(a.getPatientName()) + "\","

                + "\"address\":\""
                + escapeJson(a.getAddress()) + "\","

                + "\"contactNumber\":\""
                + escapeJson(a.getContactNumber()) + "\","

                + "\"dentistName\":\""
                + escapeJson(a.getDentistName()) + "\","

                + "\"treatmentName\":\""
                + escapeJson(a.getTreatmentName()) + "\","

                + "\"appointmentDate\":\""
                + escapeJson(a.getAppointmentDate()) + "\","

                + "\"appointmentTime\":\""
                + escapeJson(a.getAppointmentTime()) + "\","

                + "\"status\":\""
                + escapeJson(a.getStatus()) + "\""

                + "}";
    }


    private String readRequestBody(
            HttpExchange exchange
    ) throws IOException {

        ByteArrayOutputStream result =
                new ByteArrayOutputStream();

        byte[] buffer =
                new byte[1024];

        int length;


        try (InputStream inputStream =
                     exchange.getRequestBody()) {

            while ((length =
                    inputStream.read(buffer))
                    != -1) {

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
                        "\""
                                + Pattern.quote(key)
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