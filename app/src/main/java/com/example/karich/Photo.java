package com.example.karich;

public class Photo {
    private String url;

    public Photo() {
        // Пустой конструктор, необходимый для Firebase
    }

    public Photo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}