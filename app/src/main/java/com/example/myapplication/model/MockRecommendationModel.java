package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

public class MockRecommendationModel implements IRecommendationModel {

    private List<Song> mockSongs;

    public MockRecommendationModel() {
        mockSongs = new ArrayList<>();
        mockSongs.add(new Song("mock_001", "Nhạc Thư Giãn (Mock)", "AI Artist", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
        mockSongs.add(new Song("mock_002", "Giai Điệu Mới (Mock)", "AI Artist", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"));
    }

    @Override
    public void getNextSong(String currentSongId, DataCallback<Song> callback) {
        if (currentSongId == null) {
            callback.onSuccess(mockSongs.get(0));
            return;
        }
        
        for (int i = 0; i < mockSongs.size(); i++) {
            if (mockSongs.get(i).getSongId().equals(currentSongId)) {
                callback.onSuccess(mockSongs.get((i + 1) % mockSongs.size()));
                return;
            }
        }
        callback.onSuccess(mockSongs.get(0));
    }

    @Override
    public void getTopTracks(DataCallback<List<Song>> callback) {
        callback.onSuccess(mockSongs);
    }

    @Override
    public void searchSongs(String query, DataCallback<List<Song>> callback) {
        callback.onSuccess(mockSongs);
    }

    @Override
    public void getSongsByArtist(String artistName, DataCallback<List<Song>> callback) {
        callback.onSuccess(mockSongs);
    }

    @Override
    public void getRecommendations(String currentSongTitle, DataCallback<List<Song>> callback) {
        callback.onSuccess(mockSongs);
    }
}