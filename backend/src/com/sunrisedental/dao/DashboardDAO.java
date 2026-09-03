package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import java.sql.*;
import java.util.*;

public class DashboardDAO {

    public int todayAppointments() throws SQLException {
        return getCount("SELECT COUNT(*) FROM appointments WHERE appointment_date=CURDATE() AND status<>'CANCELLED'");
    }

    public int pendingToday() throws SQLException {
        return getCount("SELECT COUNT(*) FROM appointments WHERE appointment_date=CURDATE() AND status='PENDING'");
    }

    public int activePatients() throws SQLException {
        return getCount("SELECT COUNT(DISTINCT patient_id) FROM appointments WHERE status<>'CANCELLED'");
    }

    public double monthlyRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(b.total_amount),0) FROM bills b JOIN appointments a ON b.appointment_id=a.appointment_id " +
                "WHERE YEAR(a.appointment_date)=YEAR(CURDATE()) AND MONTH(a.appointment_date)=MONTH(CURDATE())";
        try (Connection c = DBConnection.getInstance().getConnection();
             Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getDouble(1) : 0;
        }
    }

    public List<String[]> todaySchedule() throws SQLException {
        String sql = "SELECT p.name,t.treatment_name,d.name,a.appointment_time FROM appointments a " +
                "JOIN patients p ON a.patient_id=p.patient_id " +
                "JOIN dentists d ON a.dentist_id=d.dentist_id " +
                "JOIN treatments t ON a.treatment_id=t.treatment_id " +
                "WHERE a.appointment_date=CURDATE() AND a.status<>'CANCELLED' ORDER BY a.appointment_time";

        return rows(sql, false);
    }

    public List<String[]> recentAppointments() throws SQLException {
        String sql = "SELECT p.name,t.treatment_name,d.name,a.appointment_time,a.status FROM appointments a " +
                "JOIN patients p ON a.patient_id=p.patient_id " +
                "JOIN dentists d ON a.dentist_id=d.dentist_id " +
                "JOIN treatments t ON a.treatment_id=t.treatment_id " +
                "ORDER BY a.appointment_id DESC LIMIT 5";

        return rows(sql, true);
    }

    public List<String[]> revenueTrend() throws SQLException {
        List<String[]> list = new ArrayList<>();

        String sql = "SELECT DATE_FORMAT(a.appointment_date,'%b') month, COALESCE(SUM(b.total_amount),0) revenue " +
                "FROM appointments a LEFT JOIN bills b ON b.appointment_id=a.appointment_id " +
                "WHERE a.appointment_date >= DATE_SUB(CURDATE(), INTERVAL 5 MONTH) " +
                "GROUP BY YEAR(a.appointment_date),MONTH(a.appointment_date) " +
                "ORDER BY YEAR(a.appointment_date),MONTH(a.appointment_date)";

        try (Connection c = DBConnection.getInstance().getConnection();
             Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next())
                list.add(new String[]{r.getString("month"), r.getString("revenue")});
        }
        return list;
    }

    private int getCount(String sql) throws SQLException {
        try (Connection c = DBConnection.getInstance().getConnection();
             Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    private List<String[]> rows(String sql, boolean status) throws SQLException {
        List<String[]> list = new ArrayList<>();

        try (Connection c = DBConnection.getInstance().getConnection();
             Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                list.add(status
                        ? new String[]{r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)}
                        : new String[]{r.getString(1), r.getString(2), r.getString(3), r.getString(4)});
            }
        }
        return list;
    }
}