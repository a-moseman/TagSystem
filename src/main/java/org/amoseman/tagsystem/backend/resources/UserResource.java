package org.amoseman.tagsystem.backend.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.amoseman.tagsystem.backend.authentication.Roles;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.user.UsernameAlreadyInUseException;
import org.amoseman.tagsystem.backend.service.UserService;
import org.amoseman.tagsystem.backend.pojo.UserCreationRequest;

import java.util.logging.Logger;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private final UserService userService;
    private final UserDAO userDAO;
    private final Meter meter;
    private final Logger logger;

    public UserResource(UserService userService, UserDAO userDAO, MetricRegistry metrics) {
        this.userService = userService;
        this.userDAO = userDAO;
        this.meter = metrics.meter("user-requests");
        this.logger = Logger.getGlobal();
    }

    @POST
    public Response request(UserCreationRequest request) {
        meter.mark();
        userService.request(request);
        logger.info(String.format("Request for account creation of %s", request.username()));
        return Response.accepted().build();
    }

    @POST
    @Path("/{username}")
    @RolesAllowed({Roles.ADMIN})
    public Response accept(@Auth User user, @PathParam("username") String username) {
        meter.mark();
        try {
            if (userService.acceptRequest(username)) {
                logger.info(String.format("Admin %s accepted account creation request of %s", user.getName(), username));
                return Response.ok().build();
            }
        } catch (UsernameAlreadyInUseException e) {
            logger.info(String.format("Admin %s failed to accepted account creation request of %s as the username is already in use", user.getName(), username));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("the username %s is already in use", username)).build();
        }
        logger.info(String.format("Admin %s failed to accepted account creation request of %s as there is no corresponding request", user.getName(), username));
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("no request corresponding to the provided username %s", username)).build();
    }

    @DELETE
    @Path("/{username}")
    @PermitAll
    public Response delete(@Auth User user, @PathParam("username") String username) {
        if (!user.getName().equals(username) && !user.getRoles().contains(Roles.ADMIN)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        meter.mark();
        try {
            userDAO.removeUser(username);
            logger.info(String.format("Admin %s deleted account %s", user.getName(), username));
            return Response.ok().build();
        }
        catch (UserDoesNotExistException e) {
            logger.info(String.format("Admin %s failed to delete account %s as it does not exist", user.getName(), username));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("%s does not exist", username)).build();
        }
    }

    @GET
    @RolesAllowed({Roles.ADMIN})
    public Response listRequests(@Auth User user) {
        meter.mark();
        logger.info(String.format("Admin %s requested a list of all pending account creation requests", user.getName()));
        return Response.ok(userService.listRequests()).build();
    }
}
