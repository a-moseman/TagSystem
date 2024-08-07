package org.amoseman.tagsystem.backend.dao.sql;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;

public class DatabaseInitializer {
    private final DatabaseConnection connection;

    public DatabaseInitializer(DatabaseConnection connection) {
        this.connection = connection;
    }

    public void init() {
        initTagsTable();
        initEntitiesTable();
        initUsersTable();
    }

    private void initTagsTable() {
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

    private void initEntitiesTable() {
        connection.context()
                .createTableIfNotExists("entities")
                .column(field("owner"), VARCHAR(36))
                .column(field("uuid"), VARCHAR(36))
                .constraints(
                        primaryKey(field("owner_uuid")),
                        unique(field("uuid"))
                )
                .execute();

        connection.context()
                .createTableIfNotExists("entity_tags")
                .column(field("entity"), VARCHAR(36))
                .column(field("tag"), VARCHAR(32))
                .constraints(
                        primaryKey(field("entity"))
                )
                .execute();

    }

    private void initUsersTable() {
        connection.context()
                .createTableIfNotExists("users")
                .column(field("username"), VARCHAR(64))
                .column(field("password"), VARCHAR(64))
                .column(field("role"), VARCHAR(64))
                .constraints(
                        primaryKey(field("username")),
                        unique(field("username"))
                )
                .execute();
    }
}
