package org.amoseman.tagsystem.backend.application;

import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseTest {
    private final TestCondition condition;

    public ResponseTest(TestCondition condition) {
        this.condition = condition;
    }

    public ResponseHandler<String> handle() {
        return response -> {
            StatusLine line = response.getStatusLine();
            String result = EntityUtils.toString(response.getEntity());
            int code = line.getStatusCode();
            boolean expected = condition.run(code);
            if (!expected) {
                System.err.println(line.getReasonPhrase());
                System.err.println(result);
            }
            assertTrue(expected);
            return result;
        };
    }
}
