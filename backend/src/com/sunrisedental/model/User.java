package com.sunrisedental.model;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private boolean active;

    public User(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = true; // default, overwritten by UserDAO from the DB value
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}