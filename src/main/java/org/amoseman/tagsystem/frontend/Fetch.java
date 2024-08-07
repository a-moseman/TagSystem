package org.amoseman.tagsystem.frontend;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Fetch {
    private final String domain;
    private final String username;
    private final String password;

    public Fetch(String domain, String username, String password) {
        this.domain = domain;
        this.username = username;
        this.password = password;
    }

    public Response call(String path, String method) {
        HttpURLConnection connection = getConnection(domain, path, method);
        try {
            int status = connection.getResponseCode();
            BufferedReader reader;
            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            String message = connection.getResponseMessage();
            String content = getContent(reader);
            return new Response(status, message, content);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String getContent(BufferedReader reader) {
        try {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            return content.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpURLConnection getConnection(String domain, String path, String method) {
        try {
            URL url = new URL(domain + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
            return connection;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
