package org.amoseman.tagsystem.backend.dao.sql;

import org.amoseman.tagsystem.backend.dao.DatabaseInitializer;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;

/**
 * A class for initializing a database.
 */
public class SQLDatabaseInitializer implements DatabaseInitializer {

    /**
     * Initialize the database.
     */
    @Override
    public void init(DatabaseConnection connection) {
        initTagsTable(connection);
        initEntitiesTable(connection);
        initUsersTable(connection);
    }

    private void initTagsTable(DatabaseConnection connection) {
        connection.context()
                .createTableIfNotExists("tags")
                .column(field("name"), VARCHAR(32))
                .constraints(
                        primaryKey(field("name")),
                        unique(field("name"))
                )
                .execute();

        connection.context()
                .createTableIfNotExists("tag_children")
                .column(field("parent"), VARCHAR(32))
                .column(field("child"), VARCHAR(32))
                .constraints(
                        primaryKey(field("parent"))
                )
                .execute();
    }

    private void initEntitiesTable(DatabaseConnection connection) {
        connection.context()
                .createTableIfNotExists("entities")
                .column(field("owner"), VARCHAR(36))
                .column(field("uuid"), VARCHAR(36))
                .constraints(
                        primaryKey(field("owner")),
                        unique(field("uuid"))
                )
                .execute();

        connection.context()
                .createTableIfNotExists("entity_tags")
                .column(field("entity"), VARCHAR(36))
                .column(field("tag"), VARCHAR(32))
                .column(field("owner"), VARCHAR(36))
                .constraints(
                        foreignKey(field("entity")).references(table("entities"), field("uuid"))
                )
                .execute();

    }

    private void initUsersTable(DatabaseConnection connection) {
        connection.context()
                .createTableIfNotExists("users")
                .column(field("username"), VARCHAR(64))
                .column(field("password"), VARCHAR(64))
                .column(field("salt"), VARCHAR(64))
                .column(field("role"), VARCHAR(64))
                .constraints(
                        primaryKey(field("username")),
                        unique(field("username"))
                )
                .execute();
    }
}
