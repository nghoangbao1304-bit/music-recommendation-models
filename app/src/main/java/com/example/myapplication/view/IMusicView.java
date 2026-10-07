package com.example.myapplication.view;

import com.example.myapplication.model.Song;
import java.util.List;

public interface IMusicView {
    void displaySongInfo(Song song);
    void updatePlayState(boolean isPlaying);
    void showError(String errorMessage);
    void updateProgress(int currentPosition, int totalDuration);
    
    // Thêm hàm hiển thị danh sách gợi ý
    void displayRecommendations(List<Song> recommendedSongs);
}