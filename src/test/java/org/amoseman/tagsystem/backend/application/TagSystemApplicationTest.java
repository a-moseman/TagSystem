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
    private static final ResponseHandler<String> successTest = new ResponseTest((code) -> code < 299).handle();
    private static final ResponseHandler<String> failTest = new ResponseTest((code) -> code > 299).handle();
    private static final ResponseHandler<String> noTest = new ResponseTest((code) -> true).handle();
    private static final ResponseHandler<String> authFailTest = new ResponseTest((code) -> 401 == code).handle();

    private static final Fetch admin = new Fetch()
            .setDomain("http://127.0.0.1:8080")
            .setAuth("admin", "admin");

    @Order(1)
    @Test
    void testTagCRUD() {
        // create
        admin.post("/tags/animal", successTest);
        admin.post("/tags/mammal", successTest);
        admin.post("/tags/feline", successTest);
        admin.post("/tags/animal", failTest);
        admin.post("/tags/mammal", failTest);
        admin.post("/tags/feline", failTest);
        // update
        admin.post("/tags/animal/mammal", successTest);
        admin.post("/tags/mammal/feline", successTest);
        admin.post("/tags/animal/mammal", failTest);
        admin.post("/tags/mammal/feline", failTest);
        // update - inheritance loop
        admin.post("/tags/mammal/animal", failTest);
        admin.post("/tags/feline/animal", failTest);
        // retrieve
        String children = admin.get("/tags/animal", successTest);
        assertTrue(children.contains("mammal"));
        assertFalse(children.contains("feline"));
        children = admin.get("/tags/mammal", successTest);
        assertTrue(children.contains("feline"));
        // delete
        admin.delete("/tags/animal", successTest);
        admin.delete("/tags/mammal", successTest);
        admin.delete("/tags/feline", successTest);
        admin.delete("/tags/animal", failTest);
        admin.delete("/tags/mammal", failTest);
        admin.delete("/tags/feline", failTest);
    }

    @Order(2)
    @Test
    void testEntityCRUD() {
        // set up
        admin.post("/tags/animal", noTest);
        admin.post("/tags/mammal", noTest);
        admin.post("/tags/feline", noTest);
        admin.post("/tags/reptile", noTest);
        admin.post("/tags/animal/mammal", noTest);
        admin.post("/tags/mammal/feline", noTest);
        admin.post("/tags/animal/reptile", noTest);
        // create
        String uuid = admin.post("/entities", successTest);
        // update
        admin.post(String.format("/entities/%s/mammal", uuid), successTest);
        admin.post(String.format("/entities/%s/mammal", uuid), failTest);
        admin.post(String.format("/entities/%s/feline", uuid), successTest); // should replace mammal with feline, as feline is more "specific"
        String tags = admin.get(String.format("/entities/%s", uuid), successTest);
        assertFalse(tags.contains("mammal"));
        assertTrue(tags.contains("feline"));
        // retrieve
        String retrieval = admin.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"animal\"]\n" + "}", successTest);
        assertTrue(retrieval.contains(uuid));
        retrieval = admin.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"mammal\"]\n" + "}", successTest);
        assertTrue(retrieval.contains(uuid));
        retrieval = admin.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"reptile\"]\n" + "}", successTest);
        assertFalse(retrieval.contains(uuid));
        // delete
        admin.delete(String.format("/entities/%s", uuid), successTest);
        admin.delete(String.format("/entities/%s", uuid), failTest);
    }

    @Order(3)
    @Test
    void testUserCRUD() {
        new Fetch().setDomain("http://127.0.0.1:8080").post("/users", "{\"username\": \"alice\", \"password\": \"bob\"}", successTest);
        String pending = admin.get("/users", successTest);
        assertTrue(pending.contains("alice"));
        admin.post("/users/alice", successTest);
        pending = admin.get("/users", successTest);
        assertFalse(pending.contains("alice"));
        admin.delete("/users/alice", successTest);
    }

    @Order(4)
    @Test
    void testInvalidCredentials() {
        new Fetch().setDomain("http://127.0.0.1:8080").post("/users", "{\"username\": \"alice\", \"password\": \"password\"}", noTest);
        new Fetch().setDomain("http://127.0.0.1:8080").post("/users", "{\"username\": \"bob\", \"password\": \"password\"}", noTest);
        admin.post("/users/alice", noTest);
        admin.post("/users/bob", noTest);

        Fetch alice = new Fetch()
                .setDomain("http://127.0.0.1:8080")
                .setAuth("alice", "password");
        Fetch bob = new Fetch()
                .setDomain("http://127.0.0.1:8080")
                .setAuth("bob", "password");

        String entity = alice.post("/entities", successTest);
        bob.get(String.format("/entities/%s", entity), authFailTest);
        admin.post("/tags/example", successTest);
        bob.post(String.format("/entities/%s/example", entity), authFailTest);
        alice.post(String.format("/entities/%s/example", entity), successTest);
        bob.delete(String.format("/entities/%s/example", entity), authFailTest);
    }

    @Order(5)
    @Test
    void testAuthNoCredentials() {
        String entity = admin.post("/entities", noTest);
        admin.post("/tags/a", noTest);
        admin.post("/tags/b", noTest);
        admin.post(String.format("/entities/%s/a", entity), noTest);

        Fetch userlessFetch = new Fetch()
                .setDomain("http://127.0.0.1:8080");
        userlessFetch.post("/entities", authFailTest);
        userlessFetch.delete(String.format("/entities/%s", entity), authFailTest);

        userlessFetch.post("/tags/example_tag", authFailTest);
        userlessFetch.post("/tags/a/b", authFailTest);
        admin.post("/tags/a/b", noTest);
        userlessFetch.delete("/tags/a/b", authFailTest);
        userlessFetch.get("/tags/a", authFailTest);
        userlessFetch.delete("/tags/a", authFailTest);

        userlessFetch.post(String.format("/entities/%s/b", entity), authFailTest);
        userlessFetch.delete(String.format("/entities/%s/a", entity), authFailTest);
        userlessFetch.get(String.format("/entities/%s", entity), authFailTest);
        userlessFetch.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"a\"]\n" + "}", authFailTest);
    }
}