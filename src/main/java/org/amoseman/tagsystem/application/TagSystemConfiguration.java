package org.amoseman.tagsystem.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import jakarta.validation.constraints.NotEmpty;

public class TagSystemConfiguration extends Configuration {
    @NotEmpty
    private String databaseURL = "tagsystem.db";
    @NotEmpty
    private String databaseUsername = "username";
    @NotEmpty
    private String databasePassword = "password";

    @JsonProperty
    public String getDatabaseURL() {
        return databaseURL;
    }

    @JsonProperty
    public String getDatabaseUsername() {
        return databaseUsername;
    }

    @JsonProperty
    public String getDatabasePassword() {
        return databasePassword;
    }
}
