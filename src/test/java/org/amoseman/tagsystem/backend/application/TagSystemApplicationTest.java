package org.amoseman.tagsystem.backend.application;

import org.amoseman.tagsystem.backend.authentication.Argon2IDConfig;
import org.amoseman.tagsystem.backend.authentication.Hasher;
import org.amoseman.tagsystem.frontend.Fetch;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
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

    private static ResponseHandler<String> successTest = response -> {
        StatusLine line = response.getStatusLine();
        int code = line.getStatusCode();
        boolean condition = code < 299;
        String result = EntityUtils.toString(response.getEntity());
        if (!condition) {
            System.err.println(line.getReasonPhrase());
            System.err.println(result);
        }

        assertTrue(condition);
        return result;
    };

    private static ResponseHandler<String> failTest = response -> {
        StatusLine line = response.getStatusLine();
        int code = line.getStatusCode();
        boolean condition = code < 299;
        if (condition) {
            System.err.println(line.getReasonPhrase());
        }
        assertFalse(condition);
        return EntityUtils.toString(response.getEntity());
    };

    private static ResponseHandler<String> noTest = response -> EntityUtils.toString(response.getEntity());

    private static Fetch fetch = new Fetch()
            .setDomain("http://127.0.0.1:8080")
            .setAuth("admin", "admin");

    @Order(1)
    @Test
    void testTagCRUD() {
        // create
        fetch.post("/tags/animal", successTest);
        fetch.post("/tags/mammal", successTest);
        fetch.post("/tags/feline", successTest);
        fetch.post("/tags/animal", failTest);
        fetch.post("/tags/mammal", failTest);
        fetch.post("/tags/feline", failTest);
        // update
        fetch.post("/tags/animal/mammal", successTest);
        fetch.post("/tags/mammal/feline", successTest);
        fetch.post("/tags/animal/mammal", failTest);
        fetch.post("/tags/mammal/feline", failTest);
        // update - inheritance loop
        fetch.post("/tags/mammal/animal", failTest);
        fetch.post("/tags/feline/animal", failTest);
        // retrieve
        String children = fetch.get("/tags/animal", successTest);
        assertTrue(children.contains("mammal"));
        assertFalse(children.contains("feline"));
        children = fetch.get("/tags/mammal", successTest);
        assertTrue(children.contains("feline"));
        // delete
        fetch.delete("/tags/animal", successTest);
        fetch.delete("/tags/mammal", successTest);
        fetch.delete("/tags/feline", successTest);
        fetch.delete("/tags/animal", failTest);
        fetch.delete("/tags/mammal", failTest);
        fetch.delete("/tags/feline", failTest);
    }

    @Order(2)
    @Test
    void testEntityCRUD() {
        fetch.post("/tags/animal", noTest);
        fetch.post("/tags/mammal", noTest);
        fetch.post("/tags/feline", noTest);
        fetch.post("/tags/reptile", noTest);
        fetch.post("/tags/animal/mammal", noTest);
        fetch.post("/tags/mammal/feline", noTest);
        fetch.post("/tags/animal/reptile", noTest);

        // create
        String uuid = fetch.post("/entities", successTest);
        // update
        fetch.post(String.format("/entities/%s/mammal", uuid), successTest);
        fetch.post(String.format("/entities/%s/mammal", uuid), failTest);
        fetch.post(String.format("/entities/%s/feline", uuid), successTest); // should replace mammal with feline, as feline is more "specific"
        String tags = fetch.get(String.format("/entities/%s", uuid), successTest);
        assertFalse(tags.contains("mammal"));
        assertTrue(tags.contains("feline"));
        // retrieve
        String retrieval = fetch.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"animal\"]\n" + "}", successTest);
        assertTrue(retrieval.contains(uuid));
        retrieval = fetch.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"mammal\"]\n" + "}", successTest);
        assertTrue(retrieval.contains(uuid));
        retrieval = fetch.get("/entities", "{\n" + "\t\"operator\": \"INTERSECTION\",\n" + "\t\"tags\": [\"reptile\"]\n" + "}", successTest);
        assertFalse(retrieval.contains(uuid));
        // delete
        fetch.delete(String.format("/entities/%s", uuid), successTest);
        fetch.delete(String.format("/entities/%s", uuid), failTest);
    }
}