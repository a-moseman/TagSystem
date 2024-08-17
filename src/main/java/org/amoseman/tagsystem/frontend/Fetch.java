package org.amoseman.tagsystem.frontend;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Fetch {
    private final CloseableHttpClient client;
    private String domain;
    private String auth;

    public Fetch() {
        this.client = HttpClientBuilder.create().build();
    }

    public Fetch setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public Fetch setAuth(String username, String password) {
        String joined = String.format("%s:%s", username, password);
        String base64 = Base64.getEncoder().encodeToString(joined.getBytes(StandardCharsets.UTF_8));
        this.auth = String.format("Basic %s", base64);
        return this;
    }

    private String formatRequest(String request) {
        return String.format("%s/%s", domain, request);
    }


    public String get(String request, ResponseHandler<String> handler) {
        HttpGet get = new HttpGet(formatRequest(request));
        get.addHeader("Authorization", auth);
        try {
            return client.execute(get, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String request, String entity, ResponseHandler<String> handler) {
        HttpGetWithEntity get = new HttpGetWithEntity();
        get.setURI(URI.create(formatRequest(request)));
        get.addHeader("Authorization", auth);
        try {
            get.setEntity(new StringEntity(entity));
            return client.execute(get, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String post(String request, ResponseHandler<String> handler) {
        HttpPost post = new HttpPost(formatRequest(request));
        post.addHeader("Authorization", auth);
        try {
            return client.execute(post, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String post(String request, String entity, ResponseHandler<String> handler) {
        HttpPost post = new HttpPost(formatRequest(request));
        post.addHeader("Authorization", auth);
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
            post.setEntity(new StringEntity(entity));
            return client.execute(post, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String delete(String request, ResponseHandler<String> handler) {
        HttpDelete delete = new HttpDelete(formatRequest(request));
        delete.addHeader("Authorization", auth);
        try {
            return client.execute(delete, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
