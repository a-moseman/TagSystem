package org.amoseman.tagsystem.backend.resources;

import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.amoseman.tagsystem.backend.authentication.Roles;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;
import org.amoseman.tagsystem.backend.service.UserService;
import org.amoseman.tagsystem.backend.service.UserCreationRequest;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private final UserService userService;
    private final UserDAO userDAO;

    public UserResource(UserService userService, UserDAO userDAO) {
        this.userService = userService;
        this.userDAO = userDAO;
    }

    @POST
    public Response request(UserCreationRequest request) {
        userService.request(request);
        return Response.accepted().build();
    }

    @POST
    @Path("/{username}")
    @RolesAllowed({Roles.ADMIN})
    public Response accept(@Auth User user, @PathParam("username") String username) {
        if (userService.acceptRequest(username)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("no request corresponding to the provided username %s", username)).build();
    }

    @DELETE
    @Path("/{username}")
    @RolesAllowed({Roles.ADMIN})
    public Response delete(@Auth User user, @PathParam("username") String username) {
        try {
            userDAO.removeUser(username);
            return Response.ok().build();
        }
        catch (UserDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("%s does not exist", username)).build();
        }
    }

    @GET
    @RolesAllowed({Roles.ADMIN})
    public Response listRequests(@Auth User user) {
        return Response.ok(userService.listRequests()).build();
    }
}
