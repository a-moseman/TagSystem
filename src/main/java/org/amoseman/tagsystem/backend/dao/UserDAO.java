package org.amoseman.tagsystem.backend.dao;

import org.amoseman.tagsystem.backend.authentication.User;

import java.util.Optional;
import java.util.Set;

public interface UserDAO {
    Optional<User> getUser(String username);
    Optional<String> getPassword(String username);

    void addUser(String username, String password);

    void setRoles(String username, Set<String> roles);
}
