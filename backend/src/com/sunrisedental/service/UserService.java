package com.sunrisedental.service;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        return userDAO.authenticate(username, password);
    }
}