package org.amoseman.tagsystem.dao.sql;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.dao.TagDAO;
import org.amoseman.tagsystem.exception.tag.NameInUseException;
import org.amoseman.tagsystem.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.pojo.Tag;

public class SQLTagDAO implements TagDAO {
    private final DatabaseConnection connection;

    public SQLTagDAO(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public String nextID() {
        return null;
    }

    @Override
    public String create(String name) throws NameInUseException {
        return null;
    }

    @Override
    public void delete(String id) throws TagDoesNotExistException {

    }

    @Override
    public Tag retrieve(String id) throws TagDoesNotExistException {
        return null;
    }

    @Override
    public void setChildren(String tagID, ImmutableList<String> childrenTagIDs) throws TagDoesNotExistException {

    }

    @Override
    public ImmutableList<String> listAll() {
        return null;
    }

    @Override
    public ImmutableList<String> tree(String id) throws TagDoesNotExistException {
        return null;
    }
}
