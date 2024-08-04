package org.amoseman.tagsystem.exception.tag;

public class TagDoesNotExistException extends Exception {
    public TagDoesNotExistException(long id) {
        super(String.format("No tag with the id %d exists", id));
    }
}
