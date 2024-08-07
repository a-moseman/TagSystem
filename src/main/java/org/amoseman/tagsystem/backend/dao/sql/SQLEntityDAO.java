package org.amoseman.tagsystem.backend.dao.sql;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.dao.TagDAO;
import org.amoseman.tagsystem.backend.exception.entity.EntityNotOwnedException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.backend.dao.EntityDAO;
import org.amoseman.tagsystem.backend.dao.SelectOperator;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SQLEntityDAO implements EntityDAO {
    private static final Table<Record> ENTITIES = table("entities");
    private final DatabaseConnection connection;
    private final TagDAO tagDAO;

    public SQLEntityDAO(DatabaseConnection connection, TagDAO tagDAO) {
        this.connection = connection;
        this.tagDAO = tagDAO;
    }

    private boolean owns(String owner, String uuid) {
        return 1 == connection.context().selectFrom(table("tags")).where(field("owner").eq(owner).and(field("uuid").eq(uuid))).fetch().size();
    }

    @Override
    public String create(String owner) {
        String uuid = UUID.randomUUID().toString();
        connection.context()
                .insertInto(
                        ENTITIES,
                        field("owner"),
                        field("uuid")
                )
                .values(owner, uuid)
                .execute();
        return uuid;
    }

    @Override
    public void remove(String owner, String uuid) throws EntityDoesNotExistException, EntityNotOwnedException {
        if (!owns(owner, uuid)) {
            throw new EntityNotOwnedException(owner, uuid);
        }
        int result = connection.context()
                .deleteFrom(ENTITIES)
                .where(field("uuid").eq(uuid))
                .execute();
        if (0 == result) {
            throw new EntityDoesNotExistException();
        }
        connection.context()
                .deleteFrom(table("entity_tags"))
                .where(field("entity").eq(uuid))
                .execute();
    }

    @Override
    public ImmutableList<String> retrieve(String owner, SelectOperator operator, ImmutableList<String> tags) throws TagDoesNotExistException {
        ImmutableList<String> effectiveTags = effectiveTags(tags);
        Condition condition = getCondition(owner, operator, effectiveTags);
        Result<Record> result = connection.context()
                .selectFrom(ENTITIES)
                .where(condition)
                .fetch();
        List<String> entities = new ArrayList<>();
        result.forEach(record -> {
            String uuid = record.get(field("uuid"), String.class);
            entities.add(uuid);
        });
        return ImmutableList.copyOf(entities);
    }

    private ImmutableList<String> effectiveTags(ImmutableList<String> rootTags) {
        List<String> tags = new ArrayList<>(rootTags);
        for (String tag : rootTags) {
            effectiveTagsHelper(tags, tag);
        }
        return ImmutableList.copyOf(tags);
    }

    private void effectiveTagsHelper(List<String> tags, String tag) {
        if (tags.contains(tag)) {
            return;
        }
        tags.add(tag);
        Result<Record> result = connection.context()
                .selectFrom(table("tag_children"))
                .where(field("parent").eq(tag))
                .fetch();
        result.forEach(record -> effectiveTagsHelper(tags, record.get(field("child"), String.class)));
    }

    private Condition getCondition(String owner, SelectOperator operator, ImmutableList<String> tags) {
        Condition condition = DSL.trueCondition();
        switch (operator) {
            case UNION -> {
                for (String tag : tags) {
                    condition = condition.or(field("tags").contains(tag));
                }
            }
            case INTERSECTION -> {
                for (String tag : tags) {
                    condition = condition.and(field("tags").contains(tag));
                }
            }
        }
        return condition.and(field("owner").eq(owner));
    }

    @Override
    public void addTag(String owner, String uuid, String tag) throws EntityDoesNotExistException, TagDoesNotExistException, EntityNotOwnedException {
        if (!owns(owner, uuid)) {
            throw new EntityNotOwnedException(owner, uuid);
        }
        if (!tagDAO.exists(tag)) {
            throw new TagDoesNotExistException(tag);
        }
        int result = connection.context()
                .insertInto(
                        table("entity_tags"),
                        field("entity"),
                        field("tag")
                )
                .values(
                        uuid,
                        tag
                )
                .execute();
        if (0 == result) {
            throw new EntityDoesNotExistException();
        }
    }

    @Override
    public void removeTag(String owner, String uuid, String tag) throws EntityDoesNotExistException, TagDoesNotExistException, EntityNotOwnedException {
        if (!owns(owner, uuid)) {
            throw new EntityNotOwnedException(owner, uuid);
        }
        if (!tagDAO.exists(tag)) {
            throw new TagDoesNotExistException(tag);
        }
        int result = connection.context()
                .deleteFrom(table("entity_tags"))
                .where(field("tag").eq(tag).and(field("entity").eq(uuid)))
                .execute();
        if (0 == result) {
            throw new EntityDoesNotExistException();
        }
    }

    @Override
    public ImmutableList<String> getTags(String owner, String uuid) throws EntityDoesNotExistException, EntityNotOwnedException {
        if (!owns(owner, uuid)) {
            throw new EntityNotOwnedException(owner, uuid);
        }
        Result<Record> result = connection.context()
                .selectFrom(table("entity_tags"))
                .where(field("entity").eq(uuid))
                .fetch();
        List<String> tags = new ArrayList<>();
        result.forEach(record -> tags.add(record.get(field("tag"), String.class)));
        return ImmutableList.copyOf(tags);
    }
}
