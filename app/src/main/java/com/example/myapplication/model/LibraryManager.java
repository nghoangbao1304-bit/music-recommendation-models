package com.example.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LibraryManager {
    private static final String PREF_NAME = "MusicLibrary";
    private static final String KEY_SONGS = "LikedSongs";
    private static final String KEY_RECENT = "RecentSongs";

    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;

    public LibraryManager(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
        this.prefs = this.context != null ? this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) : null;
        this.gson = new Gson();
    }

    // Lấy Key phân chia riêng biệt cho từng tài khoản Email
    private String getLikedKey() {
        if (context == null) return KEY_SONGS + "_default";
        SharedPreferences userPrefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String email = userPrefs.getString("CURRENT_USER_EMAIL", "default_user");
        String sanitized = email.replaceAll("[^a-zA-Z0-9]", "_");
        return KEY_SONGS + "_" + sanitized;
    }

    private String getRecentKey() {
        if (context == null) return KEY_RECENT + "_default";
        SharedPreferences userPrefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String email = userPrefs.getString("CURRENT_USER_EMAIL", "default_user");
        String sanitized = email.replaceAll("[^a-zA-Z0-9]", "_");
        return KEY_RECENT + "_" + sanitized;
    }

    // --- LỊCH SỬ NGHE GẦN ĐÂY ---
    public void addRecentSong(Song song) {
        if (song == null || prefs == null) return;
        List<Song> recents = getRecentSongs();
        
        // Xóa bài hát cũ nếu đã tồn tại để đẩy nó lên đầu danh sách
        for (int i = 0; i < recents.size(); i++) {
            if (recents.get(i).getTitle().equals(song.getTitle())) {
                recents.remove(i);
                break;
            }
        }
        
        recents.add(0, song); // Thêm vào vị trí đầu tiên
        
        // Chỉ lưu tối đa 15 bài nghe gần nhất để tránh nặng máy
        if (recents.size() > 15) {
            recents = recents.subList(0, 15);
        }
        prefs.edit().putString(getRecentKey(), gson.toJson(recents)).apply();
    }

    public List<Song> getRecentSongs() {
        if (prefs == null) return new ArrayList<>();
        String json = prefs.getString(getRecentKey(), null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Song>>(){}.getType();
        List<Song> recents = gson.fromJson(json, type);
        return recents != null ? recents : new ArrayList<>();
    }

    // --- BÀI HÁT YÊU THÍCH ---
    public List<Song> getLikedSongs() {
        if (prefs == null) return new ArrayList<>();
        String json = prefs.getString(getLikedKey(), null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Song>>(){}.getType();
        List<Song> songs = gson.fromJson(json, type);
        if (songs != null) {
            for (Song s : songs) {
                s.setConfidence(0);
            }
            return songs;
        }
        return new ArrayList<>();
    }

    // Thêm hoặc xóa bài hát khỏi thư viện
    public boolean toggleLike(Song song) {
        if (song == null || prefs == null) return false;
        List<Song> songs = getLikedSongs();
        
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getTitle().equals(song.getTitle())) {
                songs.remove(i);
                saveSongs(songs);
                return false; // Đã xóa
            }
        }
        
        // Reset confidence về 0 trước khi lưu vào thư viện
        song.setConfidence(0);
        songs.add(song);
        saveSongs(songs);
        return true; // Đã thêm
    }

    // Kiểm tra bài hát có trong thư viện không
    public boolean isLiked(Song song) {
        if (song == null) return false;
        List<Song> songs = getLikedSongs();
        for (Song s : songs) {
            if (s.getTitle().equals(song.getTitle())) return true;
        }
        return false;
    }

    private void saveSongs(List<Song> songs) {
        if (prefs == null) return;
        prefs.edit().putString(getLikedKey(), gson.toJson(songs)).apply();
    }
}