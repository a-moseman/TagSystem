package org.amoseman.tagsystem.backend.application;

import org.amoseman.tagsystem.backend.authentication.Argon2IDConfig;
import org.amoseman.tagsystem.backend.authentication.Hashing;
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

    private void generateTestConfigFile() {
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

    private void runApplication() {
        new File("test-tagsystem.db").deleteOnExit();
        TagSystemApplication application = new TagSystemApplication();
        try {
            application.run("server", "test-config.yaml");
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void initializeAdmin() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:test-tagsystem.db");
        }
        catch (SQLException e) {
            fail(e.getMessage());
        }
        Hashing hashing = new Hashing(24, 16, new SecureRandom(), new Argon2IDConfig(2, 66536, 1));
        byte[] salt = hashing.salt();
        String hash = hashing.hash("admin", salt);
        DSLContext context = DSL.using(connection, SQLDialect.SQLITE);
        context
                .insertInto(table("users"), field("username"), field("password"), field("salt"), field("role"))
                .values("admin", hash, Base64.getEncoder().encodeToString(salt), "ADMIN")
                .execute();
    }

    @Order(1)
    @Test
    void setUp() {
        generateTestConfigFile();
        runApplication();
        initializeAdmin();
    }

    private static ResponseHandler<String> test = response -> {
        StatusLine line = response.getStatusLine();
        int code = line.getStatusCode();
        if (code > 299) {
            System.out.println(line.getReasonPhrase());
        }
        assertEquals(200, code);
        return EntityUtils.toString(response.getEntity());
    };
    private static Fetch fetch = new Fetch()
            .setDomain("http://127.0.0.1:8080")
            .setAuth("admin", "admin");
    private static String uuid;

    @Order(2)
    @Test
    void testCreateEntity() {
        uuid = fetch.post("/entities", test);
    }

    @Order(3)
    @Test
    void testCreateTags() {
        fetch.post("/tags/animal", test);
        fetch.post("/tags/reptile", test);
        fetch.post("/tags/mammal", test);
        fetch.post("/tags/feline", test);
    }

    @Order(4)
    @Test
    void testTagInheritance() {
        fetch.post("/tags/animal/mammal", test);
        fetch.post("/tags/mammal/feline", test);
    }

    @Order(5)
    @Test
    void testEntityTagging() {
        fetch.post(String.format("/entities/%s/%s", uuid, "mammal"), test);
    }

    private static String json = "{\n" +
            "  \"operator\": \"INTERSECTION\",\n" +
            "  \"tags\": [\n" +
            "    \"animal\"\n" +
            "  ]\n" +
            "}";
    @Order(6)
    @Test
    void testEntityRetrieval() {
        String entities = fetch.get("/entities", json, test);
        assertTrue(entities.contains(uuid));
    }

    @Order(7)
    @Test
    void testTagDeletion() {
        fetch.delete("/tags/animal", test);
        String entities = fetch.get("/entities", json, test);
        assertFalse(entities.contains(uuid));
    }

    @Order(8)
    @Test
    void testEntityDeletion() {
        fetch.delete(String.format("/entities/%s", uuid), test);
        fetch.get(String.format("/entities/%s", uuid), response -> {
            assertEquals(401, response.getStatusLine().getStatusCode());
            return EntityUtils.toString(response.getEntity());
        });
    }
}