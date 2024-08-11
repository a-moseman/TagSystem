package org.amoseman.tagsystem.backend.application;

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
    private int passwordHashLength = 24;
    private int passwordSaltLength = 16;
    private int hashIterations = 2;
    private int hashMemory = 66536;
    private int hashParallelism = 1;

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

    public int getPasswordHashLength() {
        return passwordHashLength;
    }

    public int getPasswordSaltLength() {
        return passwordSaltLength;
    }

    public int getHashIterations() {
        return hashIterations;
    }

    public int getHashMemory() {
        return hashMemory;
    }

    public int getHashParallelism() {
        return hashParallelism;
    }
}
