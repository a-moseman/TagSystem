package org.amoseman.tagsystem.dao.sql;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private final DSLContext context;

    private DatabaseConnection(Connection connection) {
        this.context = DSL.using(connection, SQLDialect.SQLITE);
    }

    public static DatabaseConnection generate(String url) {
        try {
            Connection connection = DriverManager.getConnection(url);
            return new DatabaseConnection(connection);
        }
        catch (SQLException e) {
            return null;
        }
    }

    public DSLContext context() {
        return context;
    }
}
