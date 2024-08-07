package org.amoseman.tagsystem.backend.service;

import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UserService {
    private final UserDAO userDAO;
    private final Map<String, String> requests;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.requests = new HashMap<>();
    }

    public boolean request(UserCreationRequest request) {
        if (requests.containsKey(request.username())) {
            return false;
        }
        requests.put(request.username(), request.password());
        return true;
    }

    public Set<String> listRequests() {
        return requests.keySet();
    }

    public boolean acceptRequest(String username) {
        if (!requests.containsKey(username)) {
            return false;
        }
        String password = requests.get(username);
        userDAO.addUser(username, password);
        return true;
    }

    public Optional<User> getUser(String username) {
        return userDAO.getUser(username);
    }

    public Optional<String> getPassword(String username) {
        return userDAO.getPassword(username);
    }

    public void setRole(String username, String role) throws UserDoesNotExistException {
        userDAO.setRole(username, role);
    }
}
