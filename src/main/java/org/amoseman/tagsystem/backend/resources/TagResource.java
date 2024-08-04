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
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.backend.pojo.Tag;
import org.amoseman.tagsystem.backend.service.TagService;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.tag.NameInUseException;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
public class TagResource {
    private static final Response TAG_DNE = Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
    private final TagService tagService;

    public TagResource(TagService tagService) {
        this.tagService = tagService;
    }

    @POST
    @RolesAllowed({Roles.ADMIN})
    public Response createTag(@Auth User user, String name) {
        try {
            String id = tagService.createTag(name);
            return Response.ok(id).build();
        }
        catch (NameInUseException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag name already in use").build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({Roles.ADMIN})
    public Response deleteTag(@Auth User user, @PathParam("id") String id) {
        try {
            tagService.deleteTag(id);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
        catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Response retrieveTag(@Auth User user, @PathParam("id") String id) {
        try {
            Tag tag = tagService.retrieveTag(id);
            return Response.ok(tag).build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
    }

    @GET
    @Path("/{id}/tree")
    @PermitAll
    public Response tree(@Auth User user, @PathParam("id") String id) {
        try {
            ImmutableList<String> tree = tagService.tree(id);
            return Response.ok(tree).build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({Roles.ADMIN})
    public Response setTags(@Auth User user, @PathParam("id") String id, ImmutableList<String> children) {
        try {
            tagService.setChildren(id, children);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return TAG_DNE;
        }
    }

    @GET
    @PermitAll
    public Response list(@Auth User user) {
        ImmutableList<String> tags = tagService.listAll();
        return Response.ok(tags).build();
    }
}
