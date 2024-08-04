package org.amoseman.tagsystem.backend.resources;

import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.amoseman.tagsystem.backend.authentication.Roles;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.service.UserService;
import org.amoseman.tagsystem.backend.service.UserCreationRequest;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    public Response request(UserCreationRequest request) {
        userService.request(request);
        return Response.accepted().build();
    }

    @POST
    @Path("/{username}")
    @RolesAllowed({Roles.ADMIN})
    public Response accept(@Auth User user, String username) {
        if (userService.acceptRequest(username)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("no request corresponding to the provided username %s", username)).build();
    }

    @GET
    @RolesAllowed({Roles.ADMIN})
    public Response listRequests(@Auth User user) {
        return Response.ok(userService.listRequests()).build();
    }
}
