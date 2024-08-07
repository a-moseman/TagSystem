package org.amoseman.tagsystem.backend.resources;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.EntityDAO;
import org.amoseman.tagsystem.backend.exception.entity.EntityNotOwnedException;
import org.amoseman.tagsystem.backend.pojo.EntityRetrievalRequest;
import org.amoseman.tagsystem.backend.dao.SelectOperator;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;

import java.util.Locale;

@Path("/entities")
@Produces(MediaType.APPLICATION_JSON)
public class EntityResource {
    private final EntityDAO entityDAO;

    public EntityResource(EntityDAO entityDAO) {
        this.entityDAO = entityDAO;
    }

    @POST
    @PermitAll
    public Response createEntity(@Auth User user) {
        return Response.ok(entityDAO.create(user.getName())).build();
    }

    @DELETE
    @Path("/{uuid}")
    @PermitAll
    public Response deleteEntity(@Auth User user, @PathParam("uuid") String uuid) {
        try {
            entityDAO.remove(user.getName(), uuid);
            return Response.ok().build();
        }
        catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
        catch (EntityNotOwnedException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        }
    }


    @GET
    @PermitAll
    public Response retrieve(@Auth User user, EntityRetrievalRequest request) {
        SelectOperator operator;
        switch (request.getOperator().toUpperCase(Locale.ROOT)) {
            case "UNION" -> operator = SelectOperator.UNION;
            case "INTERSECTION" -> operator = SelectOperator.INTERSECTION;
            default -> {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "invalid select operator").build();
            }
        }
        try {
            ImmutableList<String> result = entityDAO.retrieve(user.getName(), operator, ImmutableList.copyOf(request.getTags()));
            return Response.ok(result).build();
        }
        catch (TagDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
        }
    }

    @POST
    @Path("/{uuid}/{tag}")
    public Response addTag(@Auth User user, @PathParam("uuid") String uuid, @PathParam("tag") String tag) {
        try {
            entityDAO.addTag(user.getName(), uuid, tag);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
        }
        catch (EntityNotOwnedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
    }

    @DELETE
    @Path("/{uuid}/{tag}")
    public Response removeTag(@Auth User user, @PathParam("uuid") String uuid, @PathParam("tag") String tag) {
        try {
            entityDAO.removeTag(user.getName(), uuid, tag);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
        }
        catch (EntityNotOwnedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
    }

    @GET
    @Path("/{uuid}")
    @PermitAll
    public Response getTags(@Auth User user, @PathParam("uuid") String uuid) {
        try {
            ImmutableList<String> tags = entityDAO.getTags(user.getName(), uuid);
            return Response.ok().build();
        }
        catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
        catch (EntityNotOwnedException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        }
    }
}
