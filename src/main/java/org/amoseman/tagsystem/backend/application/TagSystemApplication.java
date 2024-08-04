package org.amoseman.tagsystem.backend.application;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import org.amoseman.tagsystem.backend.authentication.BasicAuthenticator;
import org.amoseman.tagsystem.backend.authentication.BasicAuthorizer;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.EntityDAO;
import org.amoseman.tagsystem.backend.dao.TagDAO;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.dao.sql.DatabaseConnection;
import org.amoseman.tagsystem.backend.dao.sql.DatabaseInitializer;
import org.amoseman.tagsystem.backend.dao.sql.SQLEntityDAO;
import org.amoseman.tagsystem.backend.dao.sql.SQLTagDAO;
import org.amoseman.tagsystem.backend.resources.EntityResource;
import org.amoseman.tagsystem.backend.resources.TagResource;
import org.amoseman.tagsystem.backend.service.EntityService;
import org.amoseman.tagsystem.backend.service.TagService;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class TagSystemApplication extends Application<TagSystemConfiguration> {
    public static void main(String... args) throws Exception {
        new TagSystemApplication().run(args);
    }
    @Override
    public void run(TagSystemConfiguration configuration, Environment environment) throws Exception {
        DatabaseConnection connection = DatabaseConnection.generate(configuration.getDatabaseURL());
        DatabaseInitializer initializer = new DatabaseInitializer(connection);
        initializer.init();

        TagDAO tagDAO = new SQLTagDAO(connection);
        EntityDAO entityDAO = new SQLEntityDAO(connection);
        UserDAO userDAO = null;

        TagService tagService = new TagService(tagDAO, entityDAO);
        EntityService entityService = new EntityService(entityDAO);

        TagResource tagResource = new TagResource(tagService);
        EntityResource entityResource = new EntityResource(entityService);
        environment.jersey().register(tagResource);
        environment.jersey().register(entityResource);

        Authenticator<BasicCredentials, User> authenticator = new BasicAuthenticator(userDAO);
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
    }
}
