package com.example.newsguardian.data;

public class News {

    // Title
    private String title;

    // Section (eg. politics, sport, etc)
    private String section;

    // Date (String with date and time)
    private String date;

    // Authors (one or many)
    private String authors;

    // URL - link to the guardian website with the news
    private String url;

    // Thumbnail of the news - image
    private String thumbnail;

    // ID
    private String newsId;

    public News() {
    }

    public News(String title, String section, String date, String authors, String url, String thumbnail, String newsId) {
        this.title = title;
        this.section = section;
        this.date = date;
        this.authors = authors;
        this.url = url;
        this.thumbnail = thumbnail;
        this.newsId = newsId;
    }

    // Parameters' getters
    public String getTitle() {
        return title;
    }
    public String getSection() {
        return section;
    }
    public String getDate() {
        return date;
    }
    public String getAuthors() {
        return authors;
    }
    public String getUrl() {
        return url;
    }
    public String getThumbnail() {
        return thumbnail;
    }
    public String getNewsId() {
        return newsId;
    }
}

