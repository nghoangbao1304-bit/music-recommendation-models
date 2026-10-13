package com.example.myapplication.model;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {
    // Lấy danh sách toàn bộ nhạc, sắp xếp theo lượt view giảm dần (nhiều view nhất lên đầu)
    @GET("rest/v1/songs?select=*&order=mpd_playlist_count.desc")
    Call<List<Song>> getSongs(
        @Header("apikey") String apiKey,
        @Header("Authorization") String auth
    );

    // Tìm kiếm bài hát theo tên
    @GET("rest/v1/songs?select=*")
    Call<List<Song>> searchSongs(
        @Header("apikey") String apiKey,
        @Header("Authorization") String auth,
        @Query(value = "title", encoded = true) String titleQuery
    );

    // Tìm kiếm bài hát theo Ca Sĩ (Artist)
    @GET("rest/v1/songs?select=*")
    Call<List<Song>> getSongsByArtist(
        @Header("apikey") String apiKey,
        @Header("Authorization") String auth,
        @Query(value = "artist", encoded = true) String artistQuery
    );

    // Cập nhật lượt nghe (view)
    @PATCH("rest/v1/songs")
    Call<Void> updateSongViews(
        @Header("apikey") String apiKey,
        @Header("Authorization") String auth,
        @Query(value = "song_id", encoded = true) String songIdQuery, // vd: "eq.123"
        @Body Map<String, Integer> body
    );

    // API Đăng ký
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(
        @Header("apikey") String apiKey,
        @Body AuthRequest request
    );

    // API Đăng nhập
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(
        @Header("apikey") String apiKey,
        @Body AuthRequest request
    );
}