package org.amoseman.tagsystem.backend.authentication;

import java.util.Locale;

public class Roles {
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    public static boolean isValid(String role) {
        String uppercase = role.toUpperCase(Locale.ROOT);
        return ADMIN.equals(uppercase) || USER.equals(uppercase);
    }
}
