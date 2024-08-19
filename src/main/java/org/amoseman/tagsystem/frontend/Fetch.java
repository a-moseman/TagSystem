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
import java.io.UnsupportedEncodingException;
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
        byte[] bytes = joined.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.getEncoder().encodeToString(bytes);
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

    private String exec(HttpRequestBase base, ResponseHandler<String> handler) {
        handleAuth(base);
        try {
            return client.execute(base, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String get(String request, ResponseHandler<String> handler) {
        HttpGet get = new HttpGet(formatRequest(request));
        return exec(get, handler);
    }

    public String get(String request, String entity, ResponseHandler<String> handler) {
        HttpGetWithEntity get = new HttpGetWithEntity();
        get.setURI(URI.create(formatRequest(request)));
        try {
            get.setEntity(new StringEntity(entity));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return exec(get, handler);

    }

    public String post(String request, ResponseHandler<String> handler) {
        HttpPost post = new HttpPost(formatRequest(request));
        return exec(post, handler);
    }

    public String post(String request, String entity, ResponseHandler<String> handler) {
        HttpPost post = new HttpPost(formatRequest(request));
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
            post.setEntity(new StringEntity(entity));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return exec(post, handler);
    }

    public String delete(String request, ResponseHandler<String> handler) {
        HttpDelete delete = new HttpDelete(formatRequest(request));
        return exec(delete, handler);
    }
}
