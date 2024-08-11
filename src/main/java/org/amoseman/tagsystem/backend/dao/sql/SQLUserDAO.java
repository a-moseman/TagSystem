package org.amoseman.tagsystem.backend.dao.sql;

import org.amoseman.tagsystem.backend.authentication.Hashing;
import org.amoseman.tagsystem.backend.authentication.Roles;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;
import org.jooq.Record;
import org.jooq.Result;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SQLUserDAO implements UserDAO {
    private final DatabaseConnection connection;
    private final Hashing hashing;

    public SQLUserDAO(DatabaseConnection connection, Hashing hashing) {
        this.connection = connection;
        this.hashing = hashing;
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
    public Optional<byte[]> getSalt(String username) {
        Record record = getRecord(username);
        if (null == record) {
            return Optional.empty();
        }
        String saltString = record.get(field("salt"), String.class);
        byte[] salt = Base64.getDecoder().decode(saltString);
        return Optional.of(salt);
    }

    @Override
    public void addUser(String username, String password) {
        byte[] salt = hashing.salt();
        String hash = hashing.hash(password, salt);
        connection.context()
                .insertInto(table("users"), field("username"), field("password"), field("salt"), field("roles"))
                .values(username, hash, Base64.getEncoder().encodeToString(salt), Roles.USER)
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
