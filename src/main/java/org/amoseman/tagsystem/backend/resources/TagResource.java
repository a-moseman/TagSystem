package org.amoseman.tagsystem.backend.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
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

import java.util.logging.Logger;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
public class TagResource {
    private static final Response TAG_DNE = Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag does not exist").build();
    private final TagDAO tagDAO;
    private final Meter meter;
    private final Logger logger;

    public TagResource(TagDAO tagDAO, MetricRegistry metrics) {
        this.tagDAO = tagDAO;
        this.meter = metrics.meter("tag-requests");
        this.logger = Logger.getGlobal();
    }

    @POST
    @Path("/{name}")
    @RolesAllowed({Roles.ADMIN})
    public Response createTag(@Auth User user, @PathParam("name") String name) {
        meter.mark();
        try {
            tagDAO.create(name);
            logger.info(String.format("User %s created tag %s", user.getName(), name));
            return Response.ok().build();
        }
        catch (NameInUseException e) {
            logger.info(String.format("User %s failed to create tag %s as it already exists", user.getName(), name));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag name already in use").build();
        }
    }

    @DELETE
    @Path("/{name}")
    @RolesAllowed({Roles.ADMIN})
    public Response deleteTag(@Auth User user, @PathParam("name") String name) {
        meter.mark();
        try {
            tagDAO.delete(name);
            logger.info(String.format("User %s deleted tag %s", user.getName(), name));
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            logger.info(String.format("User %s failed to delete tag %s as it does not exist", user.getName(), name));
            return TAG_DNE;
        }
    }

    @POST
    @Path("/{parent}/{child}")
    @RolesAllowed({Roles.ADMIN})
    public Response addChild(@Auth User user, @PathParam("parent") String parent, @PathParam("child") String child) {
        meter.mark();
        try {
            tagDAO.addChild(parent, child);
            logger.info(String.format("User %s set tag %s to inherit tag %s", user.getName(), child, parent));
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            logger.info(String.format("User %s failed to set tag %s to inherit tag %s as one of the tags does not exist", user.getName(), child, parent));
            return TAG_DNE;
        }
        catch (TagInheritanceLoopException e) {
            logger.info(String.format("User %s failed to set tag %s to inherit tag %s as it would cause an inheritance loop", user.getName(), child, parent));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "creating this relationship would cause a tag inheritance loop").build();
        }
        catch (TagIsAlreadyChildException e) {
            logger.info(String.format("User %s set tag %s to inherit tag %s as the relationship already exists", user.getName(), child, parent));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "tag is already child").build();
        }
    }

    @DELETE
    @Path("/{parent}/{child}")
    @RolesAllowed({Roles.ADMIN})
    public Response removeChild(@Auth User user, @PathParam("name") String parent, @PathParam("child") String child) {
        meter.mark();
        try {
            logger.info(String.format("User %s removed inheritance of tag %s of tag %s", user.getName(), child, parent));
            tagDAO.removeChild(parent, child);
            return Response.ok().build();
        }
        catch (TagDoesNotExistException e) {
            logger.info(String.format("User %s failed to remove inheritance of tag %s of tag %s as one of the tags does not exist", user.getName(), child, parent));
            return TAG_DNE;
        }
        catch (TagIsNotChildException e) {
            logger.info(String.format("User %s failed to remove inheritance of tag %s of tag %s as the relationship does not exist", user.getName(), child, parent));
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "passed child tag is not child of passed parent tag").build();
        }
    }

    @GET
    @Path("/{name}")
    @PermitAll
    public Response getChildren(@Auth User user, @PathParam("name") String name) {
        meter.mark();
        try {
            logger.info(String.format("User %s received the tags that inherit tag %s", user.getName(), name));
            return Response.ok(tagDAO.getChildren(name)).build();
        }
        catch (TagDoesNotExistException e) {
            logger.info(String.format("User %s failed to receive the tags that inherit tag %s as it does not exist", user.getName(), name));
            return TAG_DNE;
        }
    }

    @GET
    @PermitAll
    public Response list(@Auth User user) {
        meter.mark();
        ImmutableList<String> tags = tagDAO.listAll();
        logger.info(String.format("User %s requested all tags", user.getName()));
        return Response.ok(tags).build();
    }
}
