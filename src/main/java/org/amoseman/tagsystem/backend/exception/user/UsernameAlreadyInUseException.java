package org.amoseman.tagsystem.backend.exception.user;

public class UsernameAlreadyInUseException extends Exception {
    public UsernameAlreadyInUseException(String username) {
        super(String.format("The username %s is already in use", username));
    }
}
