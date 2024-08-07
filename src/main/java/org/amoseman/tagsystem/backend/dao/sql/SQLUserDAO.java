package org.amoseman.tagsystem.backend.dao.sql;

import org.amoseman.tagsystem.backend.authentication.Roles;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;
import org.jooq.Record;
import org.jooq.Result;

import java.util.*;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SQLUserDAO implements UserDAO {
    private final DatabaseConnection connection;

    public SQLUserDAO(DatabaseConnection connection) {
        this.connection = connection;
    }

    private Record getRecord(String username) {
        Result<Record> result = connection.context()
                .selectFrom(table("users"))
                .where(field("username").eq(username))
                .fetch();
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public Optional<User> getUser(String username) {
        Record record = getRecord(username);
        if (null == record) {
            return Optional.empty();
        }
        String usernameString = record.get(field("username"), String.class);
        String[] rolesString = record.get(field("role"), String.class).split(",");
        Set<String> roles = new HashSet<>(List.of(rolesString));
        User user = new User(usernameString, roles);
        return Optional.of(user);
    }

    @Override
    public Optional<String> getPassword(String username) {
        Record record = getRecord(username);
        if (null == record) {
            return Optional.empty();
        }
        String password = record.get(field("password"), String.class);
        return Optional.of(password);
    }

    @Override
    public void addUser(String username, String password) {
        connection.context()
                .insertInto(table("users"), field("username"), field("password"), field("roles"))
                .values(username, password, Roles.USER)
                .execute();
    }

    @Override
    public void setRole(String username, String role) throws UserDoesNotExistException {
        int result = connection.context()
                .update(table("users"))
                .set(field("role"), role)
                .where(field("username").eq(username))
                .execute();
        if (0 == result) {
            throw new UserDoesNotExistException();
        }
    }
}
