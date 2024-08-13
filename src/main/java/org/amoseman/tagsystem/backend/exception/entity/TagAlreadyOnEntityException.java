package org.amoseman.tagsystem.backend.exception.entity;

public class TagAlreadyOnEntityException extends Exception {
    public TagAlreadyOnEntityException(String entity, String tag) {
        super(String.format("%s already has the %s tag", entity, tag));
    }
}
