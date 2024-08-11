package org.amoseman.tagsystem.backend.dao;

import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;

import java.util.Optional;

public interface UserDAO {
    Optional<User> getUser(String username);
    Optional<String> getPassword(String username);
    Optional<byte[]> getSalt(String username);
    void addUser(String username, String password);
    void setRole(String username, String role) throws UserDoesNotExistException;
}
