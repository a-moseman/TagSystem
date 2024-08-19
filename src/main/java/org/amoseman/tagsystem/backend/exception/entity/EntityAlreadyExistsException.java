package org.amoseman.tagsystem.backend.exception.entity;

public class EntityAlreadyExistsException extends Exception {
    public EntityAlreadyExistsException(String uuid) {
        super(String.format("Entity %s already exists", uuid));
    }
}
