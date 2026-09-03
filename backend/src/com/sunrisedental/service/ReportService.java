package com.sunrisedental.service;

import com.sunrisedental.dao.ReportDAO;

import java.sql.SQLException;
import java.util.List;

public class ReportService {
    private ReportDAO reportDAO = new ReportDAO();

    public String getReportJson(String from, String to, String dentistId) throws SQLException {
        String rangeFrom = (from == null || from.isEmpty()) ? "2000-01-01" : from;
        String rangeTo = (to == null || to.isEmpty()) ? "2100-01-01" : to;

        int totalAppointments = reportDAO.countAppointments(rangeFrom, rangeTo, dentistId);
        double totalRevenue = reportDAO.totalRevenue(rangeFrom, rangeTo, dentistId);
        String busiestDentist = reportDAO.busiestDentist(rangeFrom, rangeTo);
        String topTreatment = reportDAO.topTreatment(rangeFrom, rangeTo);
        List<String[]> breakdown = reportDAO.breakdown(rangeFrom, rangeTo, dentistId);

        StringBuilder rowsJson = new StringBuilder("[");
        for (int i = 0; i < breakdown.size(); i++) {
            String[] r = breakdown.get(i);
            rowsJson.append("{\"date\":\"").append(r[0])
                    .append("\",\"dentist\":\"").append(r[1])
                    .append("\",\"treatment\":\"").append(r[2])
                    .append("\",\"count\":").append(r[3])
                    .append(",\"revenue\":").append(r[4])
                    .append("}");
            if (i < breakdown.size() - 1) rowsJson.append(",");
        }
        rowsJson.append("]");

        return "{"
                + "\"totalAppointments\":" + totalAppointments + ","
                + "\"totalRevenue\":" + totalRevenue + ","
                + "\"busiestDentist\":\"" + busiestDentist + "\","
                + "\"topTreatment\":\"" + topTreatment + "\","
                + "\"breakdown\":" + rowsJson
                + "}";
    }
}