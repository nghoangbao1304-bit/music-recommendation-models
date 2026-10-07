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
    private final SharedPreferences prefs;
    private final Gson gson;

    private static final String KEY_RECENT = "RecentSongs";

    public LibraryManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // --- LỊCH SỬ NGHE GẦN ĐÂY ---
    public void addRecentSong(Song song) {
        if (song == null) return;
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
        prefs.edit().putString(KEY_RECENT, gson.toJson(recents)).apply();
    }

    public List<Song> getRecentSongs() {
        String json = prefs.getString(KEY_RECENT, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Song>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // --- BÀI HÁT YÊU THÍCH ---
    public List<Song> getLikedSongs() {
        String json = prefs.getString(KEY_SONGS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Song>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Thêm hoặc xóa bài hát khỏi thư viện
    public boolean toggleLike(Song song) {
        List<Song> songs = getLikedSongs();
        boolean isLiked = false;
        
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getTitle().equals(song.getTitle())) {
                songs.remove(i);
                saveSongs(songs);
                return false; // Đã xóa
            }
        }
        
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
        prefs.edit().putString(KEY_SONGS, gson.toJson(songs)).apply();
    }
}