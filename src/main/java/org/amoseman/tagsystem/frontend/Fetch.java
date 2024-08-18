package org.amoseman.tagsystem.frontend;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Provides HTTP request functionality.
 */
public class Fetch {
    private final CloseableHttpClient client;
    private String domain;
    private String auth;

    /**
     * Instantiate an instance of fetch.
     */
    public Fetch() {
        this.client = HttpClientBuilder.create().build();
    }

    /**
     * Set the domain of future requests.
     * @param domain the domain.
     * @return this.
     */
    public Fetch setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Set the basic credentials of future requests.
     * @param username the username.
     * @param password the password.
     * @return this.
     */
    public Fetch setAuth(String username, String password) {
        String joined = String.format("%s:%s", username, password);
        String base64 = Base64.getEncoder().encodeToString(joined.getBytes(StandardCharsets.UTF_8));
        this.auth = String.format("Basic %s", base64);
        return this;
    }

    private String formatRequest(String request) {
        return String.format("%s/%s", domain, request);
    }

    /**
     * Adds the authorization header to the provided request, if any credentials have been provided.
     * @param base the request.
     */
    private void handleAuth(HttpRequestBase base) {
        if (null == auth) {
            return;
        }
        base.addHeader("Authorization", auth);
    }


    public String get(String request, ResponseHandler<String> handler) {
        HttpGet get = new HttpGet(formatRequest(request));
        handleAuth(get);
        try {
            return client.execute(get, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String request, String entity, ResponseHandler<String> handler) {
        HttpGetWithEntity get = new HttpGetWithEntity();
        handleAuth(get);
        get.setURI(URI.create(formatRequest(request)));
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
        handleAuth(post);
        try {
            return client.execute(post, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String post(String request, String entity, ResponseHandler<String> handler) {
        HttpPost post = new HttpPost(formatRequest(request));
        handleAuth(post);
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
        handleAuth(delete);
        try {
            return client.execute(delete, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
