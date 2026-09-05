package com.sunrisedental.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Map<String, String> sessions =
            new ConcurrentHashMap<>();

    private static final String SEPARATOR = ":";


    public static String createSession(
            String username,
            String role
    ) {

        validateUsername(username);

        String token = generateToken();

        sessions.put(
                token,
                buildSessionValue(username, role)
        );

        return token;
    }


    public static boolean isValid(
            String token
    ) {

        return token != null
                && sessions.containsKey(token);
    }


    public static String getRole(
            String token
    ) {

        String value = sessions.get(token);

        return value == null
                ? null
                : extractRole(value);
    }


    public static String getUsername(
            String token
    ) {

        String value = sessions.get(token);

        return value == null
                ? null
                : extractUsername(value);
    }


    public static void invalidate(
            String token
    ) {

        if (token != null) {
            sessions.remove(token);
        }
    }


    private static void validateUsername(
            String username
    ) {

        if (username == null
                || username.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username cannot be blank."
            );
        }
    }


    private static String generateToken() {

        return UUID.randomUUID().toString();
    }


    private static String buildSessionValue(
            String username,
            String role
    ) {

        return username + SEPARATOR + role;
    }


    private static String extractUsername(
            String sessionValue
    ) {

        return sessionValue.split(SEPARATOR)[0];
    }


    private static String extractRole(
            String sessionValue
    ) {

        return sessionValue.split(SEPARATOR)[1];
    }
}