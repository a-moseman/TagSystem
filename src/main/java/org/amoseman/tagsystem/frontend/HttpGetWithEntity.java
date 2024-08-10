package org.amoseman.tagsystem.frontend;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {

    public HttpGetWithEntity() {
        addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    @Override
    public String getMethod() {
        return "GET";
    }
}
