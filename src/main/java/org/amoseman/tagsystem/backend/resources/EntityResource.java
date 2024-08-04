package org.amoseman.tagsystem.backend.resources;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.pojo.EntityRetrievalRequest;
import org.amoseman.tagsystem.backend.dao.SelectOperator;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.backend.service.EntityService;

import java.util.Locale;

@Path("/entities")
@Produces(MediaType.APPLICATION_JSON)
public class EntityResource {
    private final EntityService entityService;

    public EntityResource(EntityService entityService) {
        this.entityService = entityService;
    }

    @POST
    @PermitAll
    public Response createEntity(@Auth User user) {
        return Response.ok(entityService.create()).build();
    }

    @DELETE
    @Path("/{uuid}")
    @PermitAll
    public Response deleteEntity(@Auth User user, @PathParam("uuid") String uuid) {
        try {
            entityService.deleteEntity(uuid);
            return Response.ok().build();
        }
        catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
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
            ImmutableList<String> result = entityService.retrieveEntities(operator, ImmutableList.copyOf(request.getTags()));
            return Response.ok(result).build();
        }
        catch (TagDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
        }
    }

    @PUT
    @Path("/{uuid}")
    @PermitAll
    public Response setTags(@Auth User user, @PathParam("uuid") String uuid, ImmutableList<String> tags) {
        try {
            entityService.setTags(uuid, tags);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
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
            ImmutableList<String> tags = entityService.getTags(uuid);
            return Response.ok().build();
        }
        catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
    }
}
