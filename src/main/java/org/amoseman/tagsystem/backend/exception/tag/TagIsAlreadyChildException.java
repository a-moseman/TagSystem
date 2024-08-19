package org.amoseman.tagsystem.backend.exception.tag;

public class TagIsAlreadyChildException extends Exception {
    public TagIsAlreadyChildException(String child, String parent) {
        super(String.format("Tag %s is already a child of %s", child, parent));
    }
}
