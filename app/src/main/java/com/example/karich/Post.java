package com.example.karich;

public class Post {
    private String title;
    private String content;
    private String userId;
    private String postId;
    private long timestamp;

    public Post() {
        // Пустой конструктор необходим для Firestore
    }

    public Post(String title, String content, String userId, String postId, long timestamp) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.postId = postId;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
