package org.amoseman.tagsystem.backend.dao.sql;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.backend.dao.TagDAO;
import org.amoseman.tagsystem.backend.exception.entity.EntityNotOwnedException;
import org.amoseman.tagsystem.backend.exception.entity.TagAlreadyOnEntityException;
import org.amoseman.tagsystem.backend.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.backend.dao.EntityDAO;
import org.amoseman.tagsystem.backend.dao.RetrievalOperator;
import org.amoseman.tagsystem.backend.exception.entity.EntityDoesNotExistException;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

public class SQLEntityDAO implements EntityDAO {
    private static final Table<Record> ENTITIES = table("entities");
    private final DatabaseConnection connection;
    private final TagDAO tagDAO;

    public SQLEntityDAO(DatabaseConnection connection, TagDAO tagDAO) {
        this.connection = connection;
        this.tagDAO = tagDAO;
    }

    private boolean owns(String owner, String uuid) {
        return 1 == connection.context()
                .selectFrom(table("entities"))
                .where(field("owner").eq(owner).and(field("uuid").eq(uuid)))
                .fetch()
                .size();
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
    public ImmutableList<String> retrieve(String owner, RetrievalOperator operator, ImmutableList<String> tags) {
        ImmutableList<TagGroup> tagGroups = effectiveTags(tags);
        Condition condition = getCondition(owner, operator, tagGroups);
        Result<Record> result = connection.context()
                .selectFrom("entity_tags")
                .where(condition)
                .fetch();
        List<String> entities = new ArrayList<>();
        result.forEach(record -> {
            String uuid = record.get(field("entity"), String.class);
            entities.add(uuid);
        });
        return ImmutableList.copyOf(entities);
    }

    private ImmutableList<TagGroup> effectiveTags(ImmutableList<String> rootTags) {
        List<TagGroup> groups = new ArrayList<>();
        for (String tag : rootTags) {
            List<String> group = new ArrayList<>();
            effectiveTagsHelper(group, tag);
            groups.add(new TagGroup(ImmutableList.copyOf(group)));
        }
        return ImmutableList.copyOf(groups);
    }

    private void effectiveTagsHelper(List<String> group, String tag) {
        Result<Record> result = connection.context()
                .selectFrom(table("tag_children"))
                .where(field("parent").eq(tag))
                .fetch();
        result.forEach(record -> effectiveTagsHelper(group, record.get(field("child"), String.class)));
        if (group.contains(tag)) {
            return;
        }
        group.add(tag);
    }

    private Condition getCondition(String owner, RetrievalOperator operator, ImmutableList<TagGroup> groups) {
        Condition condition = switch (operator) {
            case UNION -> {
                condition = DSL.falseCondition();
                for (TagGroup group : groups) {
                    condition = condition.or(group.asCondition());
                }
                yield condition;
            }
            case INTERSECTION -> {
                condition = DSL.trueCondition();
                for (TagGroup group : groups) {
                    condition = condition.and(group.asCondition());
                }
                yield condition;
            }
        };
        return condition.and(field("owner").eq(owner));
    }

    private ImmutableList<String> allParents(String tag) throws TagDoesNotExistException {
        ImmutableList<String> parents = tagDAO.getParents(tag);
        List<String> list = new ArrayList<>();
        for (String parent : parents) {
            allParentsHelper(list, parent);
        }
        return ImmutableList.copyOf(list);
    }

    private void allParentsHelper(List<String> list, String tag) throws TagDoesNotExistException {
        if (list.contains(tag)) {
            return;
        }
        list.add(tag);
        ImmutableList<String> parents = tagDAO.getParents(tag);
        for (String parent : parents) {
            allParentsHelper(list, parent);
        }
    }

    @Override
    public void addTag(String owner, String uuid, String tag) throws EntityDoesNotExistException, TagDoesNotExistException, EntityNotOwnedException, TagAlreadyOnEntityException {
        if (!owns(owner, uuid)) {
            throw new EntityNotOwnedException(owner, uuid);
        }
        if (!tagDAO.exists(tag)) {
            throw new TagDoesNotExistException(tag);
        }
        if (getTags(owner, uuid).contains(tag)) {
            throw new TagAlreadyOnEntityException(uuid, tag);
        }
        ImmutableList<String> parents = allParents(tag);
        ImmutableList<String> currentTags = getTags(owner, uuid);
        List<String> toRemove = currentTags.stream().filter(parents::contains).toList();
        for (String t : toRemove) {
            removeTag(owner, uuid, t);
        }
        connection.context()
                .insertInto(
                        table("entity_tags"),
                        field("entity"),
                        field("tag"),
                        field("owner")
                )
                .values(
                        uuid,
                        tag,
                        owner
                )
                .execute();
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
