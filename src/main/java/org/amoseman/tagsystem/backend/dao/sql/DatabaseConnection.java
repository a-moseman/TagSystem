package org.amoseman.tagsystem.backend.dao.sql;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents a connection to a SQL database.
 */
public class DatabaseConnection {
    private final Connection connection;
    private final DSLContext context;

    /**
     * Instantiate a database connection.
     * @param connection the connection to use.
     */
    private DatabaseConnection(final Connection connection) {
        this.connection = connection;
        this.context = DSL.using(connection, SQLDialect.SQLITE);
    }

    /**
     * Generate a database connection using the provided URL.
     * @param url the URL of the connection.
     * @return the database connection.
     */
    public static DatabaseConnection generate(final String url) {
        try {
             Connection connection  = DriverManager.getConnection(url);
             return new DatabaseConnection(connection);
        }
        catch (SQLException e) {
            return null;
        }
    }

    /**
     * Get the connection to the database.
     * @return the connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Get the DSLContext for the database connection.
     * To be used by DAOs for SQL queries.
     * @return the context.
     */
    public DSLContext context() {
        return context;
    }
}
