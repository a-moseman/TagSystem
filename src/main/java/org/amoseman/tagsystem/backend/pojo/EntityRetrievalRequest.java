package org.amoseman.tagsystem.backend.pojo;

public class EntityRetrievalRequest {
    private final String operator;
    private final String[] tags;

    public EntityRetrievalRequest(String operator, String[] tags) {
        this.operator = operator;
        this.tags = tags;
    }

    public String getOperator() {
        return operator;
    }

    public String[] getTags() {
        return tags;
    }
}
