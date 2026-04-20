package com.yaoyao.service;

public class MusicInfo {
    private String id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private String url;
    private String cover;

    public MusicInfo() {}

    public MusicInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
}
