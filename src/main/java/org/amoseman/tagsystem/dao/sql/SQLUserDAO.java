package org.amoseman.tagsystem.dao.sql;

import org.amoseman.tagsystem.authentication.Roles;
import org.amoseman.tagsystem.authentication.User;
import org.amoseman.tagsystem.dao.UserDAO;
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
        String[] rolesString = record.get(field("roles"), String.class).split(",");
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
    public void setRoles(String username, Set<String> roles) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = roles.iterator();
        int i = 0;
        while (iterator.hasNext() && i < roles.size() - 1) {
            builder.append(iterator.next()).append(',');
        }
        builder.append(iterator.next());
        connection.context()
                .update(table("users"))
                .set(field("roles"), builder.toString())
                .where(field("username").eq(username))
                .execute();
    }
}
