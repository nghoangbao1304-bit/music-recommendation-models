package com.example.myapplication.model;

import java.util.List;

public interface IRecommendationModel {
    
    // Callback cho mạng bất đồng bộ
    interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String errorMsg);
    }

    /**
     * Lấy bài hát tiếp theo dựa trên bài hát hiện tại.
     */
    void getNextSong(String currentSongId, DataCallback<Song> callback);

    /**
     * Lấy danh sách các bài hát (Dành cho trang chủ)
     */
    void getTopTracks(DataCallback<List<Song>> callback);

    /**
     * Lấy danh sách TOP 5 gợi ý từ AI
     */
    void getRecommendations(String currentSongTitle, DataCallback<List<Song>> callback);

    /**
     * Tìm kiếm bài hát theo tên
     */
    void searchSongs(String query, DataCallback<List<Song>> callback);

    /**
     * Lấy danh sách bài hát của một Ca sĩ cụ thể
     */
    void getSongsByArtist(String artistName, DataCallback<List<Song>> callback);
}