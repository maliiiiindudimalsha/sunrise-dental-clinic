package com.sunrisedental.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Map<String, String> sessions = new ConcurrentHashMap<>();

    public static String createSession(String username, String role) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username + ":" + role);
        return token;
    }

    public static boolean isValid(String token) {
        return token != null && sessions.containsKey(token);
    }

    public static String getRole(String token) {
        String value = sessions.get(token);
        return value == null ? null : value.split(":")[1];
    }

    public static String getUsername(String token) {
        String value = sessions.get(token);
        return value == null ? null : value.split(":")[0];
    }

    public static void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}