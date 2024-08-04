package org.amoseman.tagsystem.service;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.dao.EntityDAO;
import org.amoseman.tagsystem.dao.SelectOperator;
import org.amoseman.tagsystem.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.exception.tag.TagDoesNotExistException;

public class EntityService {
    private final EntityDAO entityDAO;

    public EntityService(EntityDAO entityDAO) {
        this.entityDAO = entityDAO;
    }

    public String create() {
        return entityDAO.create();
    }

    public void deleteEntity(String uuid) throws EntityDoesNotExistException {
        entityDAO.remove(uuid);
    }

    public ImmutableList<String> retrieveEntities(SelectOperator operator, ImmutableList<String> tags) throws TagDoesNotExistException {
        return entityDAO.retrieve(operator, tags);
    }

    public void setTags(String uuid, ImmutableList<String> tags) throws TagDoesNotExistException, EntityDoesNotExistException {
        entityDAO.setTags(uuid, tags);
    }

    public ImmutableList<String> getTags(String uuid) throws EntityDoesNotExistException {
        return entityDAO.getTags(uuid);
    }
}
