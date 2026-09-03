package com.sunrisedental.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.service.TreatmentService;
import com.sunrisedental.util.SessionManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreatmentController implements HttpHandler {

    private final TreatmentService treatmentService =
            new TreatmentService();


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
                exchange
                        .getRequestURI()
                        .getPath()
                        .split("/");

        Integer id = null;


        try {

            if (parts.length > 2 &&
                    !parts[2].isBlank()) {

                id =
                        Integer.parseInt(
                                parts[2]
                        );
            }


            if ("GET".equalsIgnoreCase(method)
                    && id == null) {

                sendResponse(
                        exchange,
                        200,
                        toJsonArray(
                                treatmentService.getAll()
                        )
                );

                return;
            }


            if (!isAdmin(exchange)) {

                sendResponse(
                        exchange,
                        403,
                        "{\"status\":\"error\",\"message\":\"Admin access required\"}"
                );

                return;
            }


            if ("POST".equalsIgnoreCase(method)
                    && id == null) {

                handleAdd(exchange);

            } else if ("PUT".equalsIgnoreCase(method)
                    && id != null) {

                handleUpdate(
                        exchange,
                        id
                );

            } else if ("DELETE".equalsIgnoreCase(method)
                    && id != null) {

                boolean deleted =
                        treatmentService.delete(id);


                sendResponse(
                        exchange,
                        deleted ? 200 : 404,
                        deleted
                                ? "{\"status\":\"success\",\"message\":\"Treatment deleted successfully\"}"
                                : "{\"status\":\"error\",\"message\":\"Treatment not found\"}"
                );

            } else {

                sendResponse(
                        exchange,
                        405,
                        "{\"status\":\"error\",\"message\":\"Method not allowed\"}"
                );
            }


        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"status\":\"error\",\"message\":\"Invalid treatment ID\"}"
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
                            + escapeJson(
                            databaseMessage(e)
                    )
                            + "\"}"
            );
        }
    }


    private void handleAdd(
            HttpExchange exchange
    ) throws IOException, SQLException {

        String body =
                readRequestBody(exchange);

        Treatment treatment =
                new Treatment();


        treatment.setTreatmentName(
                extractJsonValue(
                        body,
                        "treatmentName"
                )
        );


        String fee =
                extractJsonValue(
                        body,
                        "consultationFee"
                );


        if (fee == null) {

            throw new IllegalArgumentException(
                    "Consultation fee is required."
            );
        }


        treatment.setConsultationFee(
                Double.parseDouble(fee)
        );


        Treatment saved =
                treatmentService.add(
                        treatment
                );


        sendResponse(
                exchange,
                200,
                toJson(saved)
        );
    }


    private void handleUpdate(
            HttpExchange exchange,
            int id
    ) throws IOException, SQLException {

        String body =
                readRequestBody(exchange);

        Treatment treatment =
                new Treatment();


        treatment.setTreatmentName(
                extractJsonValue(
                        body,
                        "treatmentName"
                )
        );


        String fee =
                extractJsonValue(
                        body,
                        "consultationFee"
                );


        if (fee == null) {

            throw new IllegalArgumentException(
                    "Consultation fee is required."
            );
        }


        treatment.setConsultationFee(
                Double.parseDouble(fee)
        );


        boolean updated =
                treatmentService.update(
                        id,
                        treatment
                );


        sendResponse(
                exchange,
                updated ? 200 : 404,
                updated
                        ? "{\"status\":\"success\",\"message\":\"Treatment updated successfully\"}"
                        : "{\"status\":\"error\",\"message\":\"Treatment not found\"}"
        );
    }


    private boolean isAdmin(
            HttpExchange exchange
    ) {

        String token =
                exchange
                        .getRequestHeaders()
                        .getFirst(
                                "X-Auth-Token"
                        );

        return "ADMIN".equalsIgnoreCase(
                SessionManager.getRole(token)
        );
    }


    private String toJson(
            Treatment treatment
    ) {

        return "{"
                + "\"treatmentId\":"
                + treatment.getTreatmentId()
                + ",\"treatmentName\":\""
                + escapeJson(
                treatment.getTreatmentName()
        )
                + "\",\"consultationFee\":"
                + treatment.getConsultationFee()
                + "}";
    }


    private String toJsonArray(
            List<Treatment> list
    ) {

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < list.size();
             i++) {

            json.append(
                    toJson(
                            list.get(i)
                    )
            );

            if (i < list.size() - 1) {
                json.append(",");
            }
        }

        return json
                .append("]")
                .toString();
    }


    private String readRequestBody(
            HttpExchange exchange
    ) throws IOException {

        ByteArrayOutputStream result =
                new ByteArrayOutputStream();

        byte[] buffer =
                new byte[1024];

        int length;


        try (InputStream is =
                     exchange.getRequestBody()) {

            while ((length =
                    is.read(buffer))
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


    private String escapeJson(
            String value
    ) {

        if (value == null)
            return "";

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    private String databaseMessage(
            SQLException e
    ) {

        if (e.getMessage() != null &&
                e.getMessage()
                        .toLowerCase()
                        .contains(
                                "foreign key"
                        )) {

            return "This treatment is already used by an appointment and cannot be deleted.";
        }

        return e.getMessage();
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