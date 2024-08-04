package org.amoseman.tagsystem.service;

import com.google.common.collect.ImmutableList;
import org.amoseman.tagsystem.dao.EntityDAO;
import org.amoseman.tagsystem.dao.SelectOperator;
import org.amoseman.tagsystem.dao.TagDAO;
import org.amoseman.tagsystem.exception.entity.EntityDoesNotExistException;
import org.amoseman.tagsystem.exception.tag.NameInUseException;
import org.amoseman.tagsystem.exception.tag.TagDoesNotExistException;
import org.amoseman.tagsystem.pojo.Tag;

import java.util.List;

public class TagService {
    private final TagDAO tagDAO;
    private final EntityDAO entityDAO;

    public TagService(TagDAO tagDAO, EntityDAO entityDAO) {
        this.tagDAO = tagDAO;
        this.entityDAO = entityDAO;
    }

    public String createTag(String name) throws NameInUseException {
        return tagDAO.create(name);
    }

    public void deleteTag(String id) throws TagDoesNotExistException, EntityDoesNotExistException {
        tagDAO.delete(id);
        ImmutableList<String> entitesWithTag = entityDAO.retrieve(SelectOperator.UNION, ImmutableList.of(id));
        for (String uuid : entitesWithTag) {
            ImmutableList<String> tags = entityDAO.getTags(uuid);
            List<String> filteredTags = tags.stream().filter(tag -> !tag.equals(id)).toList();
            entityDAO.setTags(uuid, ImmutableList.copyOf(filteredTags));
        }
    }

    public Tag retrieveTag(String id) throws TagDoesNotExistException {
        return tagDAO.retrieve(id);
    }

    public void setChildren(String id, ImmutableList<String> children) throws TagDoesNotExistException {
        tagDAO.setChildren(id, children);
    }

    public ImmutableList<String> listAll() {
        return tagDAO.listAll();
    }

    public ImmutableList<String> tree(String id) throws TagDoesNotExistException {
        return tagDAO.tree(id);
    }
}
