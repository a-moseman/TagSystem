package org.amoseman.tagsystem.backend.authentication;

import java.security.Principal;
import java.util.Set;

public class User implements Principal {
    private final String name;
    private final Set<String> roles;

    public User(String name, Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
