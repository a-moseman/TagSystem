package org.amoseman.tagsystem.backend.dao;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.entity.EntityNotOwnedException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;

import javax.swing.text.html.HTML;

/**
 * Represents the interface of an entity data access object.
 */
public interface EntityDAO {
    /**
     * Create a new entity.
     * @return the UUID of the entity.
     */
    String create(String owner);

    /**
     * Remove an entity.
     * @param uuid the UUID of the entity.
     * @throws EntityDoesNotExistException if the entity does not exist.
     */
    void remove(String owner, String uuid) throws EntityDoesNotExistException, EntityNotOwnedException;

    /**
     * Retrieve entities by tag.
     * @param operator the operator to use to match by either union or intersection.
     * @param tags the IDs of the tags to retrieve by.
     * @return the entities retrieved.
     */
    ImmutableList<String> retrieve(String owner, SelectOperator operator, ImmutableList<String> tags) throws TagDoesNotExistException;

    void addTag(String owner, String uuid, String tag) throws EntityDoesNotExistException, TagDoesNotExistException, EntityNotOwnedException;
    void removeTag(String owner, String uuid, String tag) throws EntityDoesNotExistException, TagDoesNotExistException, EntityNotOwnedException;

    /**
     * Get the tags of an entity.
     * @param uuid the UUID of the entity.
     * @return the tags of the entity.
     * @throws EntityDoesNotExistException if the entity does not exist.
     */
    ImmutableList<String> getTags(String owner, String uuid) throws EntityDoesNotExistException, EntityNotOwnedException;
}
