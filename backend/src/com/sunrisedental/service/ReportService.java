package com.sunrisedental.service;

import com.sunrisedental.dao.ReportDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO =
            new ReportDAO();


    public String getReportJson(
            String from,
            String to,
            String dentistId
    ) throws SQLException {

        String rangeFrom =
                validateDate(
                        from,
                        "From date"
                );

        String rangeTo =
                validateDate(
                        to,
                        "To date"
                );


        if (LocalDate.parse(rangeFrom)
                .isAfter(
                        LocalDate.parse(rangeTo)
                )) {

            throw new IllegalArgumentException(
                    "From date cannot be after To date."
            );
        }


        if (dentistId != null &&
                !dentistId.isBlank()) {

            try {

                if (Integer.parseInt(dentistId) <= 0) {

                    throw new NumberFormatException();
                }

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Invalid dentist."
                );
            }
        }


        int totalAppointments =
                reportDAO.countAppointments(
                        rangeFrom,
                        rangeTo,
                        dentistId
                );


        double totalRevenue =
                reportDAO.totalRevenue(
                        rangeFrom,
                        rangeTo,
                        dentistId
                );


        String busiestDentist =
                reportDAO.busiestDentist(
                        rangeFrom,
                        rangeTo,
                        dentistId
                );


        String topTreatment =
                reportDAO.topTreatment(
                        rangeFrom,
                        rangeTo,
                        dentistId
                );


        List<String[]> breakdown =
                reportDAO.breakdown(
                        rangeFrom,
                        rangeTo,
                        dentistId
                );


        StringBuilder rows =
                new StringBuilder("[");


        for (int i = 0;
             i < breakdown.size();
             i++) {

            String[] r =
                    breakdown.get(i);


            rows.append("{")

                    .append("\"date\":\"")
                    .append(escapeJson(r[0]))
                    .append("\",")

                    .append("\"dentist\":\"")
                    .append(escapeJson(r[1]))
                    .append("\",")

                    .append("\"treatment\":\"")
                    .append(escapeJson(r[2]))
                    .append("\",")

                    .append("\"count\":")
                    .append(r[3])
                    .append(",")

                    .append("\"revenue\":")
                    .append(r[4])

                    .append("}");


            if (i < breakdown.size() - 1) {
                rows.append(",");
            }
        }


        rows.append("]");


        return "{"
                + "\"totalAppointments\":"
                + totalAppointments
                + ","

                + "\"totalRevenue\":"
                + totalRevenue
                + ","

                + "\"busiestDentist\":\""
                + escapeJson(busiestDentist)
                + "\","

                + "\"topTreatment\":\""
                + escapeJson(topTreatment)
                + "\","

                + "\"breakdown\":"
                + rows

                + "}";
    }


    private String validateDate(
            String date,
            String fieldName
    ) {

        if (date == null ||
                date.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }


        try {

            return LocalDate
                    .parse(date)
                    .toString();

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid " +
                            fieldName.toLowerCase() +
                            "."
            );
        }
    }


    private String escapeJson(String value) {

        if (value == null)
            return "";

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}