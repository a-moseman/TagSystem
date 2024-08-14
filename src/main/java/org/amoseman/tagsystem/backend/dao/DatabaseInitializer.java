package org.amoseman.tagsystem.backend.dao;

import org.amoseman.tagsystem.backend.dao.sql.DatabaseConnection;

/**
 * Represents an interface for a database initializer.
 */
public interface DatabaseInitializer {
    /**
     * Initialize a database.
     * @param connection the connection to the database.
     */
    void init(DatabaseConnection connection);
}
