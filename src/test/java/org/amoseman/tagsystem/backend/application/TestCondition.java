package org.amoseman.tagsystem.backend.application;

/**
 * Represents a test condition for requests, based on the response code.
 */
public interface TestCondition {
    boolean run(int code);
}
