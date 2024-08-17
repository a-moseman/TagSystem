package org.amoseman.tagsystem.backend.application;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import org.amoseman.tagsystem.backend.authentication.*;
import org.amoseman.tagsystem.backend.dao.DatabaseInitializer;
import org.amoseman.tagsystem.backend.dao.EntityDAO;
import org.amoseman.tagsystem.backend.dao.TagDAO;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.dao.sql.*;
import org.amoseman.tagsystem.backend.resources.EntityResource;
import org.amoseman.tagsystem.backend.resources.TagResource;
import org.amoseman.tagsystem.backend.resources.UserResource;
import org.amoseman.tagsystem.backend.service.UserService;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.security.SecureRandom;

public class TagSystemApplication extends Application<TagSystemConfiguration> {
    public static void main(String... args) throws Exception {
        new TagSystemApplication().run(args);
    }
    @Override
    public void run(TagSystemConfiguration configuration, Environment environment) throws Exception {
        DatabaseConnection connection = DatabaseConnection.generate(configuration.getDatabaseURL());
        DatabaseInitializer initializer = new SQLDatabaseInitializer();
        initializer.init(connection);

        Hasher hasher = new Hasher(
                configuration.getPasswordHashLength(),
                configuration.getPasswordSaltLength(),
                new SecureRandom(),
                new Argon2IDConfig(
                        configuration.getHashIterations(),
                        configuration.getHashMemory(),
                        configuration.getHashParallelism())
        );

        TagDAO tagDAO = new SQLTagDAO(connection);
        EntityDAO entityDAO = new SQLEntityDAO(connection, tagDAO);
        UserDAO userDAO = new SQLUserDAO(connection, hasher);

        UserService userService = new UserService(userDAO);

        TagResource tagResource = new TagResource(tagDAO);
        EntityResource entityResource = new EntityResource(entityDAO);
        UserResource userResource = new UserResource(userService, userDAO);
        environment.jersey().register(tagResource);
        environment.jersey().register(entityResource);
        environment.jersey().register(userResource);

        Authenticator<BasicCredentials, User> authenticator = new BasicAuthenticator(userDAO, hasher);
        Authorizer<User> authorizer = new BasicAuthorizer();
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(authenticator)
                        .setAuthorizer(authorizer)
                        .setRealm("BASIC-AUTH-REALM")
                        .buildAuthFilter()
                )
        );
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));


        environment.healthChecks().register("application", new ApplicationHealthCheck(connection));
    }
}
