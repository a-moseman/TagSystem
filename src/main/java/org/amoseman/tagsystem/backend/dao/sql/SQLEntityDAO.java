package org.amoseman.tagsystem.backend.dao.sql;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.backend.dao.EntityDAO;
import org.amoseman.tagsystem.backend.dao.SelectOperator;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;

public class SQLEntityDAO implements EntityDAO {
    private final DatabaseConnection connection;

    public SQLEntityDAO(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public String create() {
        return null;
    }

    @Override
    public void remove(String uuid) throws EntityDoesNotExistException {

    }

    @Override
    public ImmutableList<String> retrieve(SelectOperator operator, ImmutableList<String> tags) throws TagDoesNotExistException {
        return null;
    }

    @Override
    public void setTags(String uuid, ImmutableList<String> tags) throws EntityDoesNotExistException, TagDoesNotExistException {

    }

    @Override
    public ImmutableList<String> getTags(String uuid) throws EntityDoesNotExistException {
        return null;
    }
}
