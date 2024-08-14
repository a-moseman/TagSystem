package org.amoseman.tagsystem.backend.dao.sql;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.tag.*;
import org.amoseman.tagsystem.backend.dao.TagDAO;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SQLTagDAO implements TagDAO {
    private static final Table<Record> TAGS_TABLE = table("tags");
    private static final Field<Object> NAME_FIELD = field("name");
    private final DatabaseConnection connection;

    public SQLTagDAO(DatabaseConnection connection) {
        this.connection = connection;
    }

    public boolean exists(String name) {
        int result = connection.context()
                .selectFrom(TAGS_TABLE)
                .where(NAME_FIELD.eq(name))
                .fetch()
                .size();
        return 1 == result;
    }

    @Override
    public void create(String name) throws NameInUseException {
        try {
            connection.context()
                    .insertInto(
                            TAGS_TABLE,
                            NAME_FIELD
                    )
                    .values(
                            name
                    )
                    .execute();
        }
        catch (Exception e) {
            throw new NameInUseException(name);
        }
    }

    @Override
    public void delete(String name) throws TagDoesNotExistException {
        int result = connection.context()
                .deleteFrom(table("tags"))
                .where(field("name").eq(name))
                .execute();
        if (0 == result) {
            throw new TagDoesNotExistException(name);
        }
        connection.context()
                .deleteFrom(table("tag_children"))
                .where(field("parent").eq(name).or(field("child").eq(name)))
                .execute();
    }

    @Override
    public ImmutableList<String> getChildren(String tag) throws TagDoesNotExistException {
        Result<Record> result = connection.context()
                .selectFrom(table("tag_children"))
                .where(field("parent").eq(tag))
                .fetch();
        List<String> children = new ArrayList<>();
        result.forEach(record -> children.add(record.get(field("child"), String.class)));
        return ImmutableList.copyOf(children);
    }

    private boolean isChild(String parent, String target) throws TagDoesNotExistException {
        ImmutableList<String> children = getChildren(parent);
        for (String child : children) {
            if (child.equals(target)) {
                return true;
            }
            if (isChild(child, target)) {
                return true;
            }
        }
        return false;
    }

    public ImmutableList<String> getParents(String tag) throws TagDoesNotExistException{
        if (!exists(tag)) {
            throw new TagDoesNotExistException(tag);
        }
        Result<Record> result = connection.context()
                .selectFrom(table("tag_children"))
                .where(field("child").eq(tag))
                .fetch();
        List<String> parents = new ArrayList<>();
        result.forEach(record -> parents.add(record.get(field("parent", String.class))));
        return ImmutableList.copyOf(parents);
    }

    @Override
    public void addChild(String parent, String child) throws TagDoesNotExistException, TagInheritanceLoopException, TagIsAlreadyChildException {
        if (!exists(parent)) {
            throw new TagDoesNotExistException(parent);
        }
        if (!exists(child)) {
            throw new TagDoesNotExistException(child);
        }
        if (isChild(child, parent)) {
            throw new TagInheritanceLoopException();
        }
        try {
            connection.context()
                    .insertInto(table("tag_children"), field("parent"), field("child"))
                    .values(parent, child)
                    .execute();
        }
        catch (Exception e) {
            throw new TagIsAlreadyChildException();
        }

    }

    @Override
    public void removeChild(String parent, String child) throws TagDoesNotExistException, TagIsNotChildException {
        if (!exists(parent)) {
            throw new TagDoesNotExistException(parent);
        }
        if (!exists(child)) {
            throw new TagDoesNotExistException(child);
        }
        int result = connection.context()
                .deleteFrom(table("tag_children"))
                .where(field("parent").eq(parent).and(field("child").eq(child)))
                .execute();
        if (0 == result){
            throw new TagIsNotChildException(parent, child);
        }
    }

    @Override
    public ImmutableList<String> listAll() {
        Result<Record> result = connection.context()
                .selectFrom(table("tags"))
                .fetch();
        List<String> tags = new ArrayList<>();
        result.forEach(record -> tags.add(record.get(field("name"), String.class)));
        return ImmutableList.copyOf(tags);
    }
}
