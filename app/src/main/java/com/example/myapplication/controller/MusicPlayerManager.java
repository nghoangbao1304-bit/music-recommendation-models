package com.example.myapplication.controller;

import android.content.Context;

import com.example.myapplication.model.SupabaseRecommendationModel;

// Singleton để chia sẻ một instance duy nhất của MusicPlayerController trên toàn App
public class MusicPlayerManager {
    private static MusicPlayerManager instance;
    private MusicPlayerController controller;

    private MusicPlayerManager(Context context) {
        // Khởi tạo controller với context của Application để tránh memory leak
        controller = new MusicPlayerController(context.getApplicationContext(), new SupabaseRecommendationModel(), null);
    }

    public static synchronized MusicPlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicPlayerManager(context);
        }
        return instance;
    }

    public MusicPlayerController getController() {
        return controller;
    }
}