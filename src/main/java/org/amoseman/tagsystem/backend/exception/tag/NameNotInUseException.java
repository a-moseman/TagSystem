package org.amoseman.tagsystem.backend.exception.tag;

public class NameNotInUseException extends Exception {
    public NameNotInUseException(String name) {
        super(String.format("No tag has the name %s", name));
    }
}
