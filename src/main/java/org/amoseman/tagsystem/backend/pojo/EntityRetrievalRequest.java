package org.amoseman.tagsystem.backend.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntityRetrievalRequest {
    private String operator;
    private String[] tags;

    public EntityRetrievalRequest() {

    }

    public EntityRetrievalRequest(String operator, String[] tags) {
        this.operator = operator;
        this.tags = tags;
    }

    @JsonProperty
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @JsonProperty
    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}
