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
     * Execute a request.
     * @param base the request.
     * @param handler the response handler.
     * @return the result.
     */
    private String exec(HttpRequestBase base, ResponseHandler<String> handler) {
        if (null != auth) {
            base.addHeader("Authorization", auth);
        }
        try {
            return client.execute(base, handler);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StringEntity tryAsEntity(String string) {
        try {
            return new StringEntity(string);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void trySetEntity(HttpRequestBase base, Class<?> clazz, StringEntity entity) {
        if (clazz.equals(HttpGetWithEntity.class)) {
            HttpGetWithEntity get = (HttpGetWithEntity) base;
            get.setEntity(entity);
        }
        else if (clazz.equals(HttpPost.class)) {
            HttpPost post = (HttpPost) base;
            post.setEntity(entity);
        }
        else {
            throw new RuntimeException("Invalid request type");
        }
    }

    private String exec(HttpRequestBase base, Class<?> clazz, String entityString, ResponseHandler<String> handler) {
        StringEntity entity = tryAsEntity(entityString);
        trySetEntity(base, clazz, entity);
        return exec(base, handler);
    }

    public String get(String request, ResponseHandler<String> handler) {
        HttpGet get = new HttpGet(formatRequest(request));
        return exec(get, handler);
    }

    public String get(String request, String entity, ResponseHandler<String> handler) {
        HttpGetWithEntity get = new HttpGetWithEntity();
        get.setURI(URI.create(formatRequest(request)));
        return exec(get, get.getClass(), entity, handler);

    }

    public String post(String request, ResponseHandler<String> handler) {
        HttpPost post = new HttpPost(formatRequest(request));
        return exec(post, handler);
    }

    public String post(String request, String entity, ResponseHandler<String> handler) {
        HttpPost post = new HttpPost(formatRequest(request));
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        return exec(post, post.getClass(), entity, handler);
    }

    public String delete(String request, ResponseHandler<String> handler) {
        HttpDelete delete = new HttpDelete(formatRequest(request));
        return exec(delete, handler);
    }
}
