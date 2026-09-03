package com.sunrisedental.service;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    // Returns the User on success, null on wrong username/password.
    // Throws IllegalArgumentException specifically when the account exists,
    // the password is correct, but the account has been deactivated -
    // LoginController turns this into a distinct, clearer error message.
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            return null;
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("This account has been deactivated. Contact an administrator.");
        }

        return user;
    }
}