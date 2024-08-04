package org.amoseman.tagsystem.exception.tag;

public class TagIsNotChildException extends Exception {
    public TagIsNotChildException(String tag, String child) {
        super(String.format("The tag %s is not the child of %s", child, tag));
    }
}
