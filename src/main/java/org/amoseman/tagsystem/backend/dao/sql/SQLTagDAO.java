package org.amoseman.tagsystem.backend.dao.sql;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.tag.TagIsNotChildException;
import org.amoseman.tagsystem.backend.dao.TagDAO;
import org.amoseman.tagsystem.backend.exception.tag.NameInUseException;
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
        int result = connection.context()
                .insertInto(
                        TAGS_TABLE,
                        NAME_FIELD
                )
                .values(
                        name
                )
                .execute();
        if (0 == result) {
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

    @Override
    public void addChild(String parent, String child) throws TagDoesNotExistException {
        if (!exists(parent)) {
            throw new TagDoesNotExistException(parent);
        }
        if (!exists(child)) {
            throw new TagDoesNotExistException(child);
        }
        connection.context()
                .insertInto(table("tag_children"), field("parent"), field("child"))
                .values(parent, child)
                .execute();
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
