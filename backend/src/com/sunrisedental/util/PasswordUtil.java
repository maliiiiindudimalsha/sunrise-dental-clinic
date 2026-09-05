package com.sunrisedental.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;


    public static String hash(String password) {

        validatePassword(password);

        try {

            byte[] salt = generateSalt();

            byte[] hash = pbkdf2(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error hashing password",
                    e
            );
        }
    }


    public static boolean verify(
            String password,
            String stored
    ) {

        try {

            String[] parts = stored.split(":");

            byte[] salt =
                    Base64.getDecoder().decode(parts[0]);

            byte[] expectedHash =
                    Base64.getDecoder().decode(parts[1]);

            byte[] actualHash = pbkdf2(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            return secureCompare(
                    actualHash,
                    expectedHash
            );

        } catch (Exception e) {

            return false;
        }
    }


    private static void validatePassword(
            String password
    ) {

        if (password == null
                || password.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Password cannot be blank."
            );
        }
    }


    private static byte[] generateSalt() {

        byte[] salt =
                new byte[SALT_LENGTH];

        new SecureRandom()
                .nextBytes(salt);

        return salt;
    }


    private static boolean secureCompare(
            byte[] actualHash,
            byte[] expectedHash
    ) {

        if (actualHash.length
                != expectedHash.length) {

            return false;
        }

        int diff = 0;

        for (int i = 0;
             i < actualHash.length;
             i++) {

            diff |= actualHash[i]
                    ^ expectedHash[i];
        }

        return diff == 0;
    }


    private static byte[] pbkdf2(
            char[] password,
            byte[] salt,
            int iterations,
            int keyLength
    ) throws Exception {

        PBEKeySpec spec =
                new PBEKeySpec(
                        password,
                        salt,
                        iterations,
                        keyLength
                );

        SecretKeyFactory skf =
                SecretKeyFactory.getInstance(
                        "PBKDF2WithHmacSHA256"
                );

        return skf
                .generateSecret(spec)
                .getEncoded();
    }
}