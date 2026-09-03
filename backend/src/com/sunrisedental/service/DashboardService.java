package com.sunrisedental.service;

import com.sunrisedental.dao.DashboardDAO;
import java.sql.SQLException;
import java.util.List;

public class DashboardService {

    private final DashboardDAO dao = new DashboardDAO();

    public String getDashboard() throws SQLException {
        return "{"
                + "\"todayAppointments\":" + dao.todayAppointments() + ","
                + "\"pendingToday\":" + dao.pendingToday() + ","
                + "\"activePatients\":" + dao.activePatients() + ","
                + "\"monthlyRevenue\":" + dao.monthlyRevenue() + ","
                + "\"schedule\":" + scheduleJson(dao.todaySchedule()) + ","
                + "\"revenueTrend\":" + trendJson(dao.revenueTrend()) + ","
                + "\"recentAppointments\":" + recentJson(dao.recentAppointments())
                + "}";
    }

    private String scheduleJson(List<String[]> rows) {
        StringBuilder j = new StringBuilder("[");
        for (String[] r : rows) {
            if (j.length() > 1) j.append(",");
            j.append("{\"patient\":\"").append(e(r[0]))
                    .append("\",\"treatment\":\"").append(e(r[1]))
                    .append("\",\"dentist\":\"").append(e(r[2]))
                    .append("\",\"time\":\"").append(e(r[3])).append("\"}");
        }
        return j.append("]").toString();
    }

    private String trendJson(List<String[]> rows) {
        StringBuilder j = new StringBuilder("[");
        for (String[] r : rows) {
            if (j.length() > 1) j.append(",");
            j.append("{\"month\":\"").append(e(r[0]))
                    .append("\",\"revenue\":").append(r[1]).append("}");
        }
        return j.append("]").toString();
    }

    private String recentJson(List<String[]> rows) {
        StringBuilder j = new StringBuilder("[");
        for (String[] r : rows) {
            if (j.length() > 1) j.append(",");
            j.append("{\"patient\":\"").append(e(r[0]))
                    .append("\",\"treatment\":\"").append(e(r[1]))
                    .append("\",\"dentist\":\"").append(e(r[2]))
                    .append("\",\"time\":\"").append(e(r[3]))
                    .append("\",\"status\":\"").append(e(r[4])).append("\"}");
        }
        return j.append("]").toString();
    }

    private String e(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}