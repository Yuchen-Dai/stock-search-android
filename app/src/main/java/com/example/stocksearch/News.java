package com.example.stocksearch;

public class News {
    private String source;
    private double date;
    private String title;
    private String des;
    private String url;
    private String website;

    public String getSource() {
        return source;
    }

    public double getDate() {
        return date;
    }

    public String getWebsite() {
        return website;
    }

    public String getTitle() {
        return title;
    }

    public String getDes() {
        return des;
    }

    public String getUrl() {
        return url;
    }

    public News(String source, double date, String title, String des, String url, String website) {
        this.source = source;
        this.date = date;
        this.title = title;
        this.des = des;
        this.url = url;
        this.website = website;
    }
}
