package org.amoseman.tagsystem.backend.dao;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.entity.EntityNotOwnedException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;

/**
 * The interface of an entity data access object.
 */
public interface EntityDAO {
    /**
     * Create a new entity.
     * @return the UUID of the entity.
     */
    String create(String owner);

    /**
     * Remove an entity.
     * @param owner the owner of the entity.
     * @param uuid the UUID of the entity.
     * @throws EntityDoesNotExistException if the entity does not exist.
     * @throws EntityNotOwnedException if the entity is not owned by the provided owner.
     */
    void remove(String owner, String uuid) throws EntityDoesNotExistException, EntityNotOwnedException;

    /**
     * Retrieve entities by tag.
     * @param owner the owner of the entities to retrieve.
     * @param operator the operator to use to match by either union or intersection.
     * @param tags the IDs of the tags to retrieve by.
     * @return the entities retrieved.
     */
    ImmutableList<String> retrieve(String owner, RetrievalOperator operator, ImmutableList<String> tags);

    /**
     * Add a tag to an entity.
     * @param owner the owner of the entity.
     * @param uuid the UUID of the entity.
     * @param tag the tag.
     * @throws EntityDoesNotExistException if the entity does not exist.
     * @throws TagDoesNotExistException if the tag does not exist.
     * @throws EntityNotOwnedException if the entity is not owned by the provided owner.
     */
    void addTag(String owner, String uuid, String tag) throws EntityDoesNotExistException, TagDoesNotExistException, EntityNotOwnedException;

    /**
     * Remove a tag from an entity.
     * @param owner the owner of the entity.
     * @param uuid the UUID of the entity.
     * @param tag the tag of the entity.
     * @throws EntityDoesNotExistException if the entity does not exist.
     * @throws TagDoesNotExistException if the tag does not exist.
     * @throws EntityNotOwnedException if the entity is not owned by the provided owner.
     */
    void removeTag(String owner, String uuid, String tag) throws EntityDoesNotExistException, TagDoesNotExistException, EntityNotOwnedException;

    /**
     * Get the tags of an entity.
     * @param owner the owner of the entity.
     * @param uuid the UUID of the entity.
     * @return the tags of the entity.
     * @throws EntityDoesNotExistException if the entity does not exist.
     * @throws EntityNotOwnedException if the entity is not owned by the provided owner.
     */
    ImmutableList<String> getTags(String owner, String uuid) throws EntityDoesNotExistException, EntityNotOwnedException;
}
