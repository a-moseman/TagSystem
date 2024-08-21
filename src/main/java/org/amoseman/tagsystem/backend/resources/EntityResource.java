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
import org.amoseman.tagsystem.backend.exception.entity.TagAlreadyOnEntityException;
import org.amoseman.tagsystem.backend.pojo.EntityRetrievalRequest;
import org.amoseman.tagsystem.backend.dao.RetrievalOperator;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;

import java.util.Locale;
import java.util.logging.Logger;

@Path("/entities")
@Produces(MediaType.APPLICATION_JSON)
public class EntityResource {
    private final EntityDAO entityDAO;
    private final Logger logger;

    public EntityResource(EntityDAO entityDAO) {
        this.entityDAO = entityDAO;
        this.logger = Logger.getGlobal();
    }

    @POST
    @PermitAll
    public Response createEntity(@Auth User user) {
        String uuid = entityDAO.create(user.getName());
        logger.info(String.format("User %s created entity %s", user.getName(), uuid));
        return Response.ok(uuid).build();
    }

    @DELETE
    @Path("/{uuid}")
    @PermitAll
    public Response deleteEntity(@Auth User user, @PathParam("uuid") String uuid) {
        try {
            entityDAO.remove(user.getName(), uuid);
            logger.info(String.format("User %s removed entity %s", user.getName(), uuid));
            return Response.ok().build();
        }
        catch (EntityDoesNotExistException e) {
            logger.info(String.format("User %s failed to remove entity %s as it does not exist", user.getName(), uuid));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
        catch (EntityNotOwnedException e) {
            logger.info(String.format("User %s failed to remove entity %s as they do not own it", user.getName(), uuid));
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        }
    }


    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response retrieve(@Auth User user, EntityRetrievalRequest request) {
        RetrievalOperator operator;
        String op = request.getOperator().toUpperCase(Locale.ROOT);
        switch (op) {
            case "UNION" -> operator = RetrievalOperator.UNION;
            case "INTERSECTION" -> operator = RetrievalOperator.INTERSECTION;
            default -> {
                logger.info(String.format("User %s failed to retrieve using the invalid operator %s", user.getName(), op));
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "invalid select operator").build();
            }
        }
        ImmutableList<String> result = entityDAO.retrieve(user.getName(), operator, ImmutableList.copyOf(request.getTags()));
        logger.info(String.format("User %s retrieved %d entities", user.getName(), result.size()));
        return Response.ok(result).build();
    }

    @POST
    @Path("/{uuid}/{tag}")
    public Response addTag(@Auth User user, @PathParam("uuid") String uuid, @PathParam("tag") String tag) {
        try {
            entityDAO.addTag(user.getName(), uuid, tag);
            logger.info(String.format("User %s added tag %s to entity %s", user.getName(), tag, uuid));
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            logger.info(String.format("User %s failed to add tag %s to entity %s as the tag does not exist", user.getName(), tag, uuid));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("tag %s does not exist", tag)).build();
        }
        catch (EntityNotOwnedException e) {
            logger.info(String.format("User %s failed to add tag %s to entity %s as they do not own the entity", user.getName(), tag, uuid));
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        catch (EntityDoesNotExistException e) {
            logger.info(String.format("User %s failed to add tag %s to entity %s as the entity does not exist", user.getName(), tag, uuid));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("entity %s does not exist", uuid)).build();
        }
        catch (TagAlreadyOnEntityException e) {
            logger.info(String.format("User %s failed to add tag %s to entity %s the entity already has the tag", user.getName(), tag, uuid));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), String.format("entity %s already has %s tag", uuid, tag)).build();
        }
    }

    @DELETE
    @Path("/{uuid}/{tag}")
    public Response removeTag(@Auth User user, @PathParam("uuid") String uuid, @PathParam("tag") String tag) {
        try {
            entityDAO.removeTag(user.getName(), uuid, tag);
            logger.info(String.format("User %s removed tag %s from entity %s", user.getName(), tag, uuid));
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            logger.info(String.format("User %s failed to remove tag %s from entity %s as the tag does not exist", user.getName(), tag, uuid));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
        }
        catch (EntityNotOwnedException e) {
            logger.info(String.format("User %s failed to remove tag %s from entity %s as they do not own the entity", user.getName(), tag, uuid));
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        catch (EntityDoesNotExistException e) {
            logger.info(String.format("User %s failed to remove tag %s from entity %s as entity does not exist", user.getName(), tag, uuid));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
    }

    @GET
    @Path("/{uuid}")
    @PermitAll
    public Response getTags(@Auth User user, @PathParam("uuid") String uuid) {
        try {
            ImmutableList<String> tags = entityDAO.getTags(user.getName(), uuid);
            logger.info(String.format("User %s retrieved tags for entity %s", user.getName(), uuid));
            return Response.ok(tags).build();
        }
        catch (EntityDoesNotExistException e) {
            logger.info(String.format("User %s failed to retrieve tags for entity %s as it does not exist", user.getName(), uuid));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "entity does not exist").build();
        }
        catch (EntityNotOwnedException e) {
            logger.info(String.format("User %s failed to retrieve tags for entity %s as they do not own it", user.getName(), uuid));
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        }
    }
}
