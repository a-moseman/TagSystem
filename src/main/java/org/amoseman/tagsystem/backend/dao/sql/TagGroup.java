package org.amoseman.tagsystem.backend.dao.sql;

import com.google.common.collect.ImmutableList;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.field;

public class TagGroup {
    private ImmutableList<String> tags;

    public TagGroup(ImmutableList<String> tags) {
        this.tags = tags;
    }

    public ImmutableList<String> getTags() {
        return tags;
    }

    public Condition asCondition() {
        Condition condition = DSL.falseCondition();
        for (String tag : tags) {
            condition = condition.or(field("tag").eq(tag));
        }
        return condition;
    }
}
