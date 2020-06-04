package com.google.sps.data;

public final class Comment {
    private final String content;
    private final long timestamp;

    public Comment(String content, long timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }
}