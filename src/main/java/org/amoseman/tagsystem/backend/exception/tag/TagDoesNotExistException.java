package org.amoseman.tagsystem.backend.exception.tag;

public class TagDoesNotExistException extends Exception {
    public TagDoesNotExistException(String name) {
        super(String.format("No tag with the name %s exists", name));
    }
}
