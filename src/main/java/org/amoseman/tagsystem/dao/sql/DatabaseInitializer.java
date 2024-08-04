package org.amoseman.tagsystem.dao.sql;

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
                .column(field("id"), BIGINTUNSIGNED.identity(true))
                .column(field("name"), VARCHAR(32))
                .column(field("children"), VARCHAR(1024))
                .column(field("parents"), VARCHAR(1024))
                .constraints(
                        primaryKey(field("id")),
                        unique(field("name"))
                )
                .execute();
    }

    private void initEntitiesTable() {
        connection.context()
                .createTableIfNotExists("entities")
                .column(field("uuid"), VARCHAR(36))
                .column(field("tags"), VARCHAR(1024))
                .execute();
    }

    private void initUsersTable() {
        connection.context()
                .createTableIfNotExists("users")
                .column(field("username"), VARCHAR(64))
                .column(field("password"), VARCHAR(64))
                .column(field("roles"), VARCHAR(64))
                .execute();
    }
}
