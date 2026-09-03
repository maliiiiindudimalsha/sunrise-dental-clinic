package com.sunrisedental.service;

import com.sunrisedental.dao.StaffDAO;
import com.sunrisedental.model.Staff;
import com.sunrisedental.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;

public class StaffService {
    private StaffDAO staffDAO = new StaffDAO();

    public List<Staff> getAll() throws SQLException {
        return staffDAO.findAll();
    }

    public String getUsernameById(int id) throws SQLException {
        return staffDAO.findUsernameById(id);
    }

    public Staff addStaff(String username, String password, String role) throws SQLException {
        validateUsername(username);
        validatePassword(password);
        validateRole(role);

        if (staffDAO.usernameExists(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }

        String hashed = PasswordUtil.hash(password);
        return staffDAO.insert(username, hashed, role.toUpperCase());
    }

    public boolean updateRole(int id, String role) throws SQLException {
        validateRole(role);
        return staffDAO.updateRole(id, role.toUpperCase());
    }

    public boolean resetPassword(int id, String newPassword) throws SQLException {
        validatePassword(newPassword);
        String hashed = PasswordUtil.hash(newPassword);
        return staffDAO.updatePassword(id, hashed);
    }

    public boolean setActive(int id, boolean active) throws SQLException {
        return staffDAO.setActive(id, active);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    private void validateRole(String role) {
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("RECEPTIONIST"))) {
            throw new IllegalArgumentException("Role must be ADMIN or RECEPTIONIST.");
        }
    }
}