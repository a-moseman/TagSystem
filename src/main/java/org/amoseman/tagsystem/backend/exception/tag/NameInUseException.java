package org.amoseman.tagsystem.backend.exception.tag;

public class NameInUseException extends Exception {
    public NameInUseException(String name) {
        super(String.format("A tag already uses the name %s", name));
    }
}
