package org.amoseman.tagsystem.backend.resources;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.amoseman.tagsystem.backend.authentication.Roles;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.TagDAO;
import org.amoseman.tagsystem.backend.exception.tag.*;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
public class TagResource {
    private static final Response TAG_DNE = Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
    private final TagDAO tagDAO;

    public TagResource(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    @POST
    @Path("/{name}")
    @RolesAllowed({Roles.ADMIN})
    public Response createTag(@Auth User user, @PathParam("name") String name) {
        try {
            tagDAO.create(name);
            return Response.ok().build();
        }
        catch (NameInUseException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag name already in use").build();
        }
    }

    @DELETE
    @Path("/{name}")
    @RolesAllowed({Roles.ADMIN})
    public Response deleteTag(@Auth User user, @PathParam("name") String name) {
        try {
            tagDAO.delete(name);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
    }

    @POST
    @Path("/{parent}/{child}")
    @RolesAllowed({Roles.ADMIN})
    public Response addChild(@Auth User user, @PathParam("parent") String parent, @PathParam("child") String child) {
        try {
            tagDAO.addChild(parent, child);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
        catch (TagInheritanceLoopException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "creating this relationship would cause a tag inheritance loop").build();
        }
        catch (TagIsAlreadyChildException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag is already child").build();
        }
    }

    @DELETE
    @Path("/{parent}/{child}")
    @RolesAllowed({Roles.ADMIN})
    public Response removeChild(@Auth User user, @PathParam("name") String parent, @PathParam("child") String child) {
        try {
            tagDAO.removeChild(parent, child);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
        catch (TagIsNotChildException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "passed child tag is not child of passed parent tag").build();
        }
    }

    @GET
    @Path("/{name}")
    @PermitAll
    public Response getChildren(@Auth User user, @PathParam("name") String name) {
        try {
            return Response.ok(tagDAO.getChildren(name)).build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
    }

    @GET
    @PermitAll
    public Response list(@Auth User user) {
        ImmutableList<String> tags = tagDAO.listAll();
        return Response.ok(tags).build();
    }
}
