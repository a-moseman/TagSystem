package org.amoseman.tagsystem.backend.dao.sql;

import org.amoseman.tagsystem.backend.authentication.Hasher;
import org.amoseman.tagsystem.backend.authentication.Roles;
import org.amoseman.tagsystem.backend.authentication.User;
import org.amoseman.tagsystem.backend.dao.UserDAO;
import org.amoseman.tagsystem.backend.exception.user.InvalidRoleException;
import org.amoseman.tagsystem.backend.exception.user.UserDoesNotExistException;
import org.amoseman.tagsystem.backend.exception.user.UsernameAlreadyInUseException;
import org.jooq.Record;
import org.jooq.Result;

import java.util.*;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SQLUserDAO implements UserDAO {
    private final DatabaseConnection connection;
    private final Hasher hasher;

    public SQLUserDAO(DatabaseConnection connection, Hasher hasher) {
        this.connection = connection;
        this.hasher = hasher;
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
    public User getUser(String username) throws UserDoesNotExistException {
        Record record = getRecord(username);
        if (null == record) {
            throw new UserDoesNotExistException(username);
        }
        String usernameString = record.get(field("username"), String.class);
        String[] rolesString = record.get(field("role"), String.class).split(",");
        Set<String> roles = new HashSet<>(List.of(rolesString));
        return new User(usernameString, roles);
    }

    @Override
    public String getPassword(String username) throws UserDoesNotExistException {
        Record record = getRecord(username);
        if (null == record) {
            throw new UserDoesNotExistException(username);
        }
        return record.get(field("password"), String.class);
    }

    @Override
    public byte[] getSalt(String username) throws UserDoesNotExistException {
        Record record = getRecord(username);
        if (null == record) {
            throw new UserDoesNotExistException(username);
        }
        String saltString = record.get(field("salt"), String.class);
        return Base64.getDecoder().decode(saltString);
    }

    @Override
    public void addUser(String username, String password) throws UsernameAlreadyInUseException {
        byte[] salt = hasher.salt();
        String hash = hasher.hash(password, salt);
        try {
            connection.context()
                    .insertInto(table("users"), field("username"), field("password"), field("salt"), field("role"))
                    .values(username, hash, Base64.getEncoder().encodeToString(salt), Roles.USER)
                    .execute();
        }
        catch (Exception e) {
            throw new UsernameAlreadyInUseException(username);
        }
    }

    @Override
    public void removeUser(String username) throws UserDoesNotExistException {
        int result = connection.context()
                .deleteFrom(table("users"))
                .where(field("username").eq(username))
                .execute();
        if (0 == result) {
            throw new UserDoesNotExistException(username);
        }

    }

    @Override
    public void setRole(String username, String role) throws UserDoesNotExistException, InvalidRoleException {
        if (!Roles.isValid(role)) {
            throw new InvalidRoleException(username, role);
        }
        int result = connection.context()
                .update(table("users"))
                .set(field("role"), role)
                .where(field("username").eq(username))
                .execute();
        if (0 == result) {
            throw new UserDoesNotExistException(username);
        }
    }
}
