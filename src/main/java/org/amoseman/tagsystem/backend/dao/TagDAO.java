package org.amoseman.tagsystem.backend.dao;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.tag.*;

/**
 * The interface of a tag data access object.
 */
public interface TagDAO {

    /**
     * Check if a tag exists.
     * @param name the name of the tag.
     * @return the result of the check.
     */
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
     * Set the child tag to inherit the parent tag.
     * @param parent the parent tag.
     * @param child the child tag.
     * @throws TagDoesNotExistException if either tag does not exist.
     * @throws TagInheritanceLoopException if this would result in an inheritance loop.
     * @throws TagIsAlreadyChildException if the inheritance already exists.
     */
    void addChild(String parent, String child) throws TagDoesNotExistException, TagInheritanceLoopException, TagIsAlreadyChildException;

    /**
     * Remove the inheritance of the child tag of the parent tag.
     * @param parent the parent tag.
     * @param child the child tag.
     * @throws TagDoesNotExistException if either tag does not exist.
     * @throws TagIsNotChildException if the child tag does not inherit the parent tag.
     */
    void removeChild(String parent, String child) throws TagDoesNotExistException, TagIsNotChildException;

    /**
     * Get all tags with directly inherit the tag.
     * @param tag the tag.
     * @return the children.
     * @throws TagDoesNotExistException if the tag does not exist.
     */
    ImmutableList<String> getChildren(String tag) throws TagDoesNotExistException;


    /**
     * List all tags.
     * @return all tags.
     */
    ImmutableList<String> listAll();
}
