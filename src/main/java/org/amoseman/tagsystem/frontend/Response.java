package org.amoseman.tagsystem.frontend;

public class Response {
    private int status;
    private String message;
    private String content;

    public Response(int status, String message, String content) {
        this.status = status;
        this.message = message;
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return String.format("Status: %d %s\nContent: %s", status, message, content);
    }
}
