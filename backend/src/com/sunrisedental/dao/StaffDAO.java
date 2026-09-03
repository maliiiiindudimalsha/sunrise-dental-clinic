package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Staff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public List<Staff> findAll() throws SQLException {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT staff_id, username, role, is_active FROM users ORDER BY staff_id";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String findUsernameById(int id) throws SQLException {
        String sql = "SELECT username FROM users WHERE staff_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public Staff insert(String username, String hashedPassword, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, is_active) VALUES (?, ?, ?, 1)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, role);
            ps.executeUpdate();

            Staff staff = new Staff();
            staff.setUsername(username);
            staff.setRole(role);
            staff.setActive(true);

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) staff.setStaffId(rs.getInt(1));
            }
            return staff;
        }
    }

    public boolean updateRole(int id, String role) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE staff_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int id, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE staff_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean setActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE users SET is_active = ? WHERE staff_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setUsername(rs.getString("username"));
        s.setRole(rs.getString("role"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    }
}