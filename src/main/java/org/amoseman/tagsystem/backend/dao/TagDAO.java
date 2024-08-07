package org.amoseman.tagsystem.backend.dao;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.tag.TagIsNotChildException;
import org.amoseman.tagsystem.backend.exception.tag.NameInUseException;

/**
 * Represents the interface of a tag data access object.
 */
public interface TagDAO {

    boolean exists(final String name);
    /**
     * Create a new tag.
     * @param name the name of the tag.
     * @throws NameInUseException if the name is already in use.
     */
    void create(final String name) throws NameInUseException;

    /**
     * Delete an existing tag.
     * @param name the name of the tag.
     * @throws TagDoesNotExistException if the tag does not exist.
     */
    void delete(final String name) throws TagDoesNotExistException;

    /**
     * Add a child tag to a tag.
      * @param parent the parent tag.
     * @param child the child tag.
     * @throws TagDoesNotExistException if either tag does not exist.
     */
    void addChild(String parent, String child) throws TagDoesNotExistException;

    /**
     * Remove a child tag from a parent tag.
     * @param parent the parent tag.
     * @param child the child tag.
     * @throws TagDoesNotExistException if either tag does not exist.
     * @throws TagIsNotChildException if the child tag is not actual a child of the parent tag.
     */
    void removeChild(String parent, String child) throws TagDoesNotExistException, TagIsNotChildException;

    /**
     * Get the first-descendant children tags of the tag.
     * @param tag the tag.
     * @return the children.
     * @throws TagDoesNotExistException if the tag does not exist.
     */
    ImmutableList<String> getChildren(String tag) throws TagDoesNotExistException;

    /**
     * List all tag ids.
     * @return the tag ids.
     */
    ImmutableList<String> listAll();
}
