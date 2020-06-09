package com.google.sps.data;

public final class Post {
    private final String message;
    private final String url;
    private final String display;

    public Post(String message, String url, String display) {
        this.message = message;
        this.url = url;
        this.display = display;
    }
}
