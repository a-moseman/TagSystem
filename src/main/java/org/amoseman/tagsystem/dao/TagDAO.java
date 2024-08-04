package org.amoseman.tagsystem.dao;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.exception.tag.NameInUseException;
import org.amoseman.tagsystem.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.pojo.Tag;

/**
 * Represents the interface of a tag data access object.
 */
public interface TagDAO {
    /**
     * Generate the next id.
     * @return the id.
     */
    String nextID();

    /**
     * Create a new tag.
     * @param name the name of the tag.
     * @return the id of the new tag.
     * @throws NameInUseException if the name is already in use.
     */
    String create(final String name) throws NameInUseException;

    /**
     * Delete an existing tag.
     * @param id the id of the tag.
     * @throws TagDoesNotExistException if the tag does not exist.
     */
    void delete(final String id) throws TagDoesNotExistException;

    /**
     * Retrieve a tag.
     * @param id the id of the tag.
     * @return the tag.
     * @throws TagDoesNotExistException if the tag does not exist.
     */
    Tag retrieve(final String id) throws TagDoesNotExistException;

    /**
     * Add a child tag to the tag.
     * @param tagID the id of the tag.
     * @param childrenTagIDs the child tag ids.
     * @throws TagDoesNotExistException if the tag or child tag does not exist.
     */
    void setChildren(final String tagID, final ImmutableList<String> childrenTagIDs) throws TagDoesNotExistException;

    /**
     * List all tag ids.
     * @return the tag ids.
     */
    ImmutableList<String> listAll();

    /**
     * List the tree of child tags beneath the provided tag.
     * @param id the id of the tag.
     * @return the ids of the child tags in the tree beneath the tag.
     */
    ImmutableList<String> tree(final String id) throws TagDoesNotExistException;
}
