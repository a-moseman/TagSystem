package org.amoseman.tagsystem.backend.dao;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;

/**
 * Represents the interface of an entity data access object.
 */
public interface EntityDAO {
    /**
     * Create a new entity.
     * @return the UUID of the entity.
     */
    String create();

    /**
     * Remove an entity.
     * @param uuid the UUID of the entity.
     * @throws EntityDoesNotExistException if the entity does not exist.
     */
    void remove(String uuid) throws EntityDoesNotExistException;

    /**
     * Retrieve entities by tag.
     * @param operator the operator to use to match by either union or intersection.
     * @param tags the IDs of the tags to retrieve by.
     * @return the entities retrieved.
     */
    ImmutableList<String> retrieve(SelectOperator operator, ImmutableList<String> tags) throws TagDoesNotExistException;

    /**
     * Set the tags of the entity.
     * @param uuid the UUID of the entity.
     * @param tags the tags to tag the entity with.
     * @throws EntityDoesNotExistException if the entity does not exist.
     * @throws TagDoesNotExistException if one of the tags do not exist.
     */
    void setTags(String uuid, ImmutableList<String> tags) throws EntityDoesNotExistException, TagDoesNotExistException;

    /**
     * Get the tags of an entity.
     * @param uuid the UUID of the entity.
     * @return the tags of the entity.
     * @throws EntityDoesNotExistException if the entity does not exist.
     */
    ImmutableList<String> getTags(String uuid) throws EntityDoesNotExistException;
}
