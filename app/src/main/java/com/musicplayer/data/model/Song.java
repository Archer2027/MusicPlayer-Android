package com.musicplayer.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "songs")
public class Song {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "album")
    private String album;

    @ColumnInfo(name = "duration")
    private long duration;

    @ColumnInfo(name = "album_art_path")
    private String albumArtPath;

    public Song() {}

    public Song(String filePath, String title, String artist,
                String album, long duration, String albumArtPath) {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.albumArtPath = albumArtPath;
    }

    // Getters
    public long getId() { return id; }
    public String getFilePath() { return filePath; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public long getDuration() { return duration; }
    public String getAlbumArtPath() { return albumArtPath; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setDuration(long duration) { this.duration = duration; }
    public void setAlbumArtPath(String albumArtPath) { this.albumArtPath = albumArtPath; }
}
