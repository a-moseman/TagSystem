package org.amoseman.tagsystem.backend.exception.user;

public class InvalidRoleException extends Exception {
    public InvalidRoleException(String username, String role) {
        super(String.format("Cannot assign user %s the invalid role %s", username, role));
    }
}
