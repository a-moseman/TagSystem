package org.amoseman.tagsystem.backend.pojo;

import com.google.common.collect.ImmutableList;

public class Tag {
    private final String id;
    private final String name;
    private final ImmutableList<String> childIDs;
    private final ImmutableList<Long> parentIDs;

    public Tag(String id, String name, ImmutableList<String> childIDs, ImmutableList<Long> parentIDs) {
        this.id = id;
        this.name = name;
        this.childIDs = childIDs;
        this.parentIDs = parentIDs;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ImmutableList<String> getChildIDs() {
        return childIDs;
    }

    public ImmutableList<Long> getParentIDs() {
        return parentIDs;
    }
}
