package org.amoseman.tagsystem.backend.exception.entity;

public class EntityNotOwnedException extends Exception {
    public EntityNotOwnedException(String owner, String uuid) {
        super(String.format("User %s does not own entity %s", owner, uuid));
    }
}
