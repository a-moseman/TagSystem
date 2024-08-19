package org.amoseman.tagsystem.backend.exception.entity;

public class EntityDoesNotExistException extends Exception {
    public EntityDoesNotExistException(String uuid) {
        super(String.format("Entity %s does not exist", uuid));
    }
}
