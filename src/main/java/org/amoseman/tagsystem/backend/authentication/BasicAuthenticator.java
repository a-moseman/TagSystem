package org.amoseman.tagsystem.backend.authentication;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;

import java.util.Optional;

public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {
    private final UserDAO userDAO;
    private final Hasher hasher;

    public BasicAuthenticator(UserDAO userDAO, Hasher hasher) {
        this.userDAO = userDAO;
        this.hasher = hasher;
    }

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        User user;
        String expectedPassword;
        byte[] salt;
        try {
            String username = credentials.getUsername();
            user = userDAO.getUser(username);
            expectedPassword = userDAO.getPassword(username);
            salt = userDAO.getSalt(username);
        }
        catch (UserDoesNotExistException e) {
            return Optional.empty();
        }
        String attemptedPassword = credentials.getPassword();
        if (!hasher.verify(attemptedPassword, salt, expectedPassword)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
