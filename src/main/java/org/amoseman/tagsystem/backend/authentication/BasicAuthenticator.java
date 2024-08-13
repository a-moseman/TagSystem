package org.amoseman.tagsystem.backend.authentication;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.amoseman.tagsystem.backend.dao.UserDAO;

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
        Optional<User> maybeUser = userDAO.getUser(credentials.getUsername());
        if (maybeUser.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> maybePassword = userDAO.getPassword(credentials.getUsername());
        Optional<byte[]> maybeSalt = userDAO.getSalt(credentials.getUsername());
        if (maybePassword.isEmpty() || maybeSalt.isEmpty()) {
            return Optional.empty();
        }
        User user = maybeUser.get();
        String password = maybePassword.get();
        byte[] salt = maybeSalt.get();
        if (!hasher.verify(credentials.getPassword(), salt, password)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
