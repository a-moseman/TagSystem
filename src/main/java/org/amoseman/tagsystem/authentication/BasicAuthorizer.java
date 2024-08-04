package org.amoseman.tagsystem.authentication;

import io.dropwizard.auth.Authorizer;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

public class BasicAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User user, String role, @Nullable ContainerRequestContext containerRequestContext) {
        Set<String> roles = user.getRoles();
        return null != roles && roles.contains(role);
    }
}
