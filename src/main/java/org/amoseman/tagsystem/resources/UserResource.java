package org.amoseman.tagsystem.resources;

import io.dropwizard.auth.Auth;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.amoseman.tagsystem.authentication.Roles;
import org.amoseman.tagsystem.authentication.User;
import org.amoseman.tagsystem.service.UserCreationRequest;
import org.amoseman.tagsystem.service.UserService;

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
