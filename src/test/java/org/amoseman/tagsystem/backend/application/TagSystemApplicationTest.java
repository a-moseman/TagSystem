package org.amoseman.tagsystem.backend.application;

import org.amoseman.tagsystem.backend.authentication.Argon2IDConfig;
import org.amoseman.tagsystem.backend.authentication.Hasher;
import org.amoseman.tagsystem.frontend.Fetch;
import org.apache.http.client.ResponseHandler;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.*;

import java.io.*;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;

import static org.jooq.impl.DSL.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TagSystemApplicationTest {

    static void generateTestConfigFile() {
        File testConfigFile = new File("test-config.yaml");
        testConfigFile.deleteOnExit();
        try {
            testConfigFile.createNewFile();
        }
        catch (IOException e) {
            fail(e.getMessage());
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testConfigFile)))){
            writer.write("databaseURL: jdbc:sqlite:test-tagsystem.db\ndatabaseUsername: username\ndatabasePassword: password");
        }
        catch (IOException e) {
            fail(e.getMessage());
        }
    }

    static void runApplication() {
        new File("test-tagsystem.db").deleteOnExit();
        TagSystemApplication application = new TagSystemApplication();
        try {
            application.run("server", "test-config.yaml");
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

    static void initializeAdmin() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:test-tagsystem.db");
        }
        catch (SQLException e) {
            fail(e.getMessage());
        }
        Hasher hasher = new Hasher(24, 16, new SecureRandom(), new Argon2IDConfig(2, 66536, 1));
        byte[] salt = hasher.salt();
        String hash = hasher.hash("admin", salt);
        DSLContext context = DSL.using(connection, SQLDialect.SQLITE);
        context
                .insertInto(table("users"), field("username"), field("password"), field("salt"), field("role"))
                .values("admin", hash, Base64.getEncoder().encodeToString(salt), "ADMIN")
                .execute();
    }

    @BeforeAll
    static void setUp() {
        generateTestConfigFile();
        runApplication();
        initializeAdmin();
    }
    private static final Fetch admin = new Fetch()
            .setDomain("http://127.0.0.1:8080")
            .setAuth("admin", "admin");

    @Order(1)
    @Test
    void testTagCRUD() {
        // create
        admin.post("/tags/animal", ResponseTest.SUCCESS.handle());
        admin.post("/tags/mammal", ResponseTest.SUCCESS.handle());
        admin.post("/tags/feline", ResponseTest.SUCCESS.handle());
        admin.post("/tags/animal", ResponseTest.FAILURE.handle());
        admin.post("/tags/mammal", ResponseTest.FAILURE.handle());
        admin.post("/tags/feline", ResponseTest.FAILURE.handle());
        // update
        admin.post("/tags/animal/mammal", ResponseTest.SUCCESS.handle());
        admin.post("/tags/mammal/feline", ResponseTest.SUCCESS.handle());
        admin.post("/tags/animal/mammal", ResponseTest.FAILURE.handle());
        admin.post("/tags/mammal/feline", ResponseTest.FAILURE.handle());
        // update - inheritance loop
        admin.post("/tags/mammal/animal", ResponseTest.FAILURE.handle());
        admin.post("/tags/feline/animal", ResponseTest.FAILURE.handle());
        // retrieve
        String children = admin.get("/tags/animal", ResponseTest.SUCCESS.handle());
        assertTrue(children.contains("mammal"));
        assertFalse(children.contains("feline"));
        children = admin.get("/tags/mammal", ResponseTest.SUCCESS.handle());
        assertTrue(children.contains("feline"));
        // delete
        admin.delete("/tags/animal", ResponseTest.SUCCESS.handle());
        admin.delete("/tags/mammal", ResponseTest.SUCCESS.handle());
        admin.delete("/tags/feline", ResponseTest.SUCCESS.handle());
        admin.delete("/tags/animal", ResponseTest.FAILURE.handle());
        admin.delete("/tags/mammal", ResponseTest.FAILURE.handle());
        admin.delete("/tags/feline", ResponseTest.FAILURE.handle());
    }

    @Order(2)
    @Test
    void testEntityCRUD() {
        // set up
        admin.post("/tags/animal", ResponseTest.NONE.handle());
        admin.post("/tags/mammal", ResponseTest.NONE.handle());
        admin.post("/tags/feline", ResponseTest.NONE.handle());
        admin.post("/tags/reptile", ResponseTest.NONE.handle());
        admin.post("/tags/animal/mammal", ResponseTest.NONE.handle());
        admin.post("/tags/mammal/feline", ResponseTest.NONE.handle());
        admin.post("/tags/animal/reptile", ResponseTest.NONE.handle());
        // create
        String uuid = admin.post("/entities", ResponseTest.SUCCESS.handle());
        // update
        admin.post(String.format("/entities/%s/mammal", uuid), ResponseTest.SUCCESS.handle());
        admin.post(String.format("/entities/%s/mammal", uuid), ResponseTest.FAILURE.handle());
        admin.post(String.format("/entities/%s/feline", uuid), ResponseTest.SUCCESS.handle()); // should replace mammal with feline, as feline is more "specific"
        String tags = admin.get(String.format("/entities/%s", uuid), ResponseTest.SUCCESS.handle());
        assertFalse(tags.contains("mammal"));
        assertTrue(tags.contains("feline"));
        // retrieve
        String retrieval = admin.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"animal\"]\n" + "}", ResponseTest.SUCCESS.handle());
        assertTrue(retrieval.contains(uuid));
        retrieval = admin.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"mammal\"]\n" + "}", ResponseTest.SUCCESS.handle());
        assertTrue(retrieval.contains(uuid));
        retrieval = admin.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"reptile\"]\n" + "}", ResponseTest.SUCCESS.handle());
        assertFalse(retrieval.contains(uuid));
        // delete
        admin.delete(String.format("/entities/%s", uuid), ResponseTest.SUCCESS.handle());
        admin.delete(String.format("/entities/%s", uuid), ResponseTest.FAILURE.handle());
    }

    @Order(3)
    @Test
    void testUserCRUD() {
        new Fetch().setDomain("http://127.0.0.1:8080").post("/users", "{\"username\": \"alice\", \"password\": \"bob\"}", ResponseTest.SUCCESS.handle());
        String pending = admin.get("/users", ResponseTest.SUCCESS.handle());
        assertTrue(pending.contains("alice"));
        admin.post("/users/alice", ResponseTest.SUCCESS.handle());
        pending = admin.get("/users", ResponseTest.SUCCESS.handle());
        assertFalse(pending.contains("alice"));
        admin.delete("/users/alice", ResponseTest.SUCCESS.handle());
    }

    @Order(4)
    @Test
    void testInvalidCredentials() {
        new Fetch().setDomain("http://127.0.0.1:8080").post("/users", "{\"username\": \"alice\", \"password\": \"password\"}", ResponseTest.NONE.handle());
        new Fetch().setDomain("http://127.0.0.1:8080").post("/users", "{\"username\": \"bob\", \"password\": \"password\"}", ResponseTest.NONE.handle());
        admin.post("/users/alice", ResponseTest.NONE.handle());
        admin.post("/users/bob", ResponseTest.NONE.handle());

        Fetch alice = new Fetch()
                .setDomain("http://127.0.0.1:8080")
                .setAuth("alice", "password");
        Fetch bob = new Fetch()
                .setDomain("http://127.0.0.1:8080")
                .setAuth("bob", "password");

        String entity = alice.post("/entities", ResponseTest.SUCCESS.handle());
        bob.get(String.format("/entities/%s", entity), ResponseTest.AUTH_FAILURE.handle());
        admin.post("/tags/example", ResponseTest.SUCCESS.handle());
        bob.post(String.format("/entities/%s/example", entity), ResponseTest.AUTH_FAILURE.handle());
        alice.post(String.format("/entities/%s/example", entity), ResponseTest.SUCCESS.handle());
        bob.delete(String.format("/entities/%s/example", entity), ResponseTest.AUTH_FAILURE.handle());
    }

    @Order(5)
    @Test
    void testAuthNoCredentials() {
        String entity = admin.post("/entities", ResponseTest.NONE.handle());
        admin.post("/tags/a", ResponseTest.NONE.handle());
        admin.post("/tags/b", ResponseTest.NONE.handle());
        admin.post(String.format("/entities/%s/a", entity), ResponseTest.NONE.handle());

        Fetch userlessFetch = new Fetch()
                .setDomain("http://127.0.0.1:8080");
        userlessFetch.post("/entities", ResponseTest.AUTH_FAILURE.handle());
        userlessFetch.delete(String.format("/entities/%s", entity), ResponseTest.AUTH_FAILURE.handle());

        userlessFetch.post("/tags/example_tag", ResponseTest.AUTH_FAILURE.handle());
        userlessFetch.post("/tags/a/b", ResponseTest.AUTH_FAILURE.handle());
        admin.post("/tags/a/b", ResponseTest.NONE.handle());
        userlessFetch.delete("/tags/a/b", ResponseTest.AUTH_FAILURE.handle());
        userlessFetch.get("/tags/a", ResponseTest.AUTH_FAILURE.handle());
        userlessFetch.delete("/tags/a", ResponseTest.AUTH_FAILURE.handle());

        userlessFetch.post(String.format("/entities/%s/b", entity), ResponseTest.AUTH_FAILURE.handle());
        userlessFetch.delete(String.format("/entities/%s/a", entity), ResponseTest.AUTH_FAILURE.handle());
        userlessFetch.get(String.format("/entities/%s", entity), ResponseTest.AUTH_FAILURE.handle());
        userlessFetch.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"a\"]\n" + "}", ResponseTest.AUTH_FAILURE.handle());
    }
}