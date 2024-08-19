package org.amoseman.tagsystem.backend.application;

import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import static org.junit.jupiter.api.Assertions.fail;

public class ResponseTest {
    public static final ResponseTest SUCCESS = new ResponseTest(code -> code < 299);
    public static final ResponseTest FAILURE = new ResponseTest(code -> code > 399 && code < 499);
    public static final ResponseTest AUTH_FAILURE = new ResponseTest(code -> 401 == code);
    public static final ResponseTest NONE = new ResponseTest(code -> true);
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
                fail(String.format("%d %s\n%s\n", code, line.getReasonPhrase(), result));
            }
            return result;
        };
    }
}
