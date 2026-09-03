package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Looks up a user by username only - the password hash is verified
    // separately in UserService, since verification needs the raw salt+hash
    // comparison logic, not a plain SQL equality check.
    public User findByUsername(String username) {
        User user = null;
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            rs.getInt("staff_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}