package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Song implements Serializable {
    
    // Khớp với tên cột trong database Supabase
    @SerializedName("song_id")
    private String songId;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("artist")
    private String artist;
    
    @SerializedName("audio_url")
    private String audioUrl;

    // Bổ sung điểm confidence (xác suất) từ AI để hiển thị lên màn hình
    private double confidence = 0.0;

    public Song(String songId, String title, String artist, String audioUrl) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.audioUrl = audioUrl;
    }

    public String getSongId() { return songId; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAudioUrl() { return audioUrl; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}