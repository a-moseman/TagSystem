package org.amoseman.tagsystem.backend.dao;

import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.exception.user.InvalidRoleException;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.user.UsernameAlreadyInUseException;

public interface UserDAO {
    User getUser(String username) throws UserDoesNotExistException;
    String getPassword(String username) throws UserDoesNotExistException;
    byte[] getSalt(String username) throws UserDoesNotExistException;
    void addUser(String username, String password) throws UsernameAlreadyInUseException;
    void removeUser(String username) throws UserDoesNotExistException;
    void setRole(String username, String role) throws UserDoesNotExistException, InvalidRoleException;
}
