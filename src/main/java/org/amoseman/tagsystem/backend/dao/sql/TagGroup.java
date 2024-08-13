package org.amoseman.tagsystem.backend.dao.sql;

import com.google.common.collect.ImmutableList;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.field;

/**
 * Represents a group of tags.
 * They are all grouped by a parent tag with they inherit.
 */
public class TagGroup {
    private final ImmutableList<String> tags;

    /**
     * Instantiate a new tag group.
     * @param tags the tags in the group.
     */
    public TagGroup(final ImmutableList<String> tags) {
        this.tags = tags;
    }

    /**
     * Get the tags within the group.
     * @return the tags within the group.
     */
    public ImmutableList<String> getTags() {
        return tags;
    }

    /**
     * Convert the tag group into a jooq condition for use in entity retrieval.
     * @return the corresponding condition.
     */
    public Condition asCondition() {
        Condition condition = DSL.falseCondition();
        for (String tag : tags) {
            condition = condition.or(field("tag").eq(tag));
        }
        return condition;
    }
}
