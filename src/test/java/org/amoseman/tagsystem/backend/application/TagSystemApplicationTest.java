package org.amoseman.tagsystem.backend.application;

import org.amoseman.tagsystem.frontend.Fetch;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.jooq.impl.DSL.*;
import static org.junit.jupiter.api.Assertions.*;

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
        DSLContext context = DSL.using(connection, SQLDialect.SQLITE);
        context
                .insertInto(table("users"), field("username"), field("password"), field("role"))
                .values("admin", "admin", "ADMIN")
                .execute();
    }
    @BeforeEach
    void setUp() {
        generateTestConfigFile();
        runApplication();
        initializeAdmin();
    }

    @Test
    void run() {
        ResponseHandler<String> test = response -> {
            StatusLine line = response.getStatusLine();
            int code = line.getStatusCode();
            if (code > 299) {
                System.out.println(line.getReasonPhrase());
            }
            assertEquals(200, code);
            return EntityUtils.toString(response.getEntity());
        };

        Fetch fetch = new Fetch()
                .setDomain("http://127.0.0.1:8080")
                .setAuth("admin", "admin");


        // create entity
        String uuid = fetch.post("/entities", test);
        // create tags
        fetch.post("/tags/animal", test);
        fetch.post("/tags/reptile", test);
        fetch.post("/tags/mammal", test);
        fetch.post("/tags/feline", test);
        // set parent/child relationships
        fetch.post("/tags/animal/mammal", test);
        fetch.post("/tags/mammal/feline", test);
        // tag entity
        fetch.post(String.format("/entities/%s/%s", uuid, "mammal"), test);
        // retrieval
        String json = "{\n" +
                "  \"operator\": \"INTERSECTION\",\n" +
                "  \"tags\": [\n" +
                "    \"animal\"\n" +
                "  ]\n" +
                "}";
        String entities = fetch.get("/entities", json, test);
        assertTrue(entities.contains(uuid));
        // tag deletion
        fetch.delete("/tags/animal", test);
        entities = fetch.get("/entities", json, test);
        assertFalse(entities.contains(uuid));
        // entity deletion
        fetch.delete(String.format("/entities/%s", uuid), test);
        fetch.get(String.format("/entities/%s", uuid), response -> {
            assertEquals(401, response.getStatusLine().getStatusCode());
            return EntityUtils.toString(response.getEntity());
        });
    }
}