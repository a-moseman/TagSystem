package org.amoseman.tagsystem.backend.application;

import com.codahale.metrics.health.HealthCheck;
import org.amoseman.tagsystem.backend.dao.sql.DatabaseConnection;

public class ApplicationHealthCheck extends HealthCheck {
    private final DatabaseConnection connection;

    public ApplicationHealthCheck(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    protected Result check() throws Exception {
        // todo: do a more proper health check
        if (connection.getConnection().isClosed()) {
            return Result.unhealthy("database connection is closed");
        }
        return Result.healthy();
    }
}
