package org.amoseman.tagsystem.backend.dao;

import org.amoseman.tagsystem.backend.dao.sql.DatabaseConnection;

public abstract class DatabaseInitializer {
    /**
     * Initialize a database.
     * @param connection the connection to the database.
     */
    public abstract void init(DatabaseConnection connection);
}
