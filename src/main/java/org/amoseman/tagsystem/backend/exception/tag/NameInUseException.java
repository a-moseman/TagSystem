package org.amoseman.tagsystem.backend.exception.tag;

public class NameInUseException extends Exception {
    public NameInUseException(long id, String name) {
        super(String.format("The tag with id %d already used the name %s", id, name));
    }
}
