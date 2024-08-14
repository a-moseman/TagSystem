package org.amoseman.tagsystem.backend.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates a request for entity retrieval.
 */
public class EntityRetrievalRequest {
    private String operator;
    private String[] tags;

    /**
     * Instantiate an empty entity retrieval request.
     */
    public EntityRetrievalRequest() {

    }

    /**
     * Instantiate an entity retrieval request.
     * @param operator the operator to use.
     * @param tags the tags to use.
     */
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
