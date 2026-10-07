package com.example.myapplication.model;

import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseRecommendationModel implements IRecommendationModel {

    private static final String SUPABASE_URL = "https://evasrqslsnyuegstaqgg.supabase.co/";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_u829setPIGPBm7F-4EI5XA_MoWyIkoR";

    private SupabaseApi api;
    private List<Song> cachedSongs;

    public SupabaseRecommendationModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SupabaseApi.class);
    }

    @Override
    public void getTopTracks(DataCallback<List<Song>> callback) {
        String bearerAuth = "Bearer " + SUPABASE_ANON_KEY;
        api.getSongs(SUPABASE_ANON_KEY, bearerAuth).enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cachedSongs = response.body();
                    callback.onSuccess(cachedSongs);
                } else {
                    callback.onError("Lỗi API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    @Override
    public void getNextSong(String currentSongTitle, DataCallback<Song> callback) {
        getRecommendations(currentSongTitle, new DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                if (songs != null && !songs.isEmpty()) {
                    callback.onSuccess(songs.get(0));
                } else {
                    callback.onError("AI không trả về bài hát");
                }
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void getRecommendations(String currentSongTitle, DataCallback<List<Song>> callback) {
        if (cachedSongs == null || cachedSongs.isEmpty()) {
            getTopTracks(new DataCallback<List<Song>>() {
                @Override
                public void onSuccess(List<Song> songs) {
                    getRecommendations(currentSongTitle, callback);
                }
                @Override
                public void onError(String errorMsg) {
                    callback.onError(errorMsg);
                }
            });
            return;
        }

        if (currentSongTitle == null || currentSongTitle.isEmpty()) {
            callback.onSuccess(cachedSongs);
            return;
        }

        Retrofit aiRetrofit = new Retrofit.Builder()
                .baseUrl("http://189.189.20.101:8080/") // IP mạng LAN của bạn
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AiRecommendationApi aiApi = aiRetrofit.create(AiRecommendationApi.class);

        aiApi.getNextSongFromAI(currentSongTitle).enqueue(new Callback<AiRecommendationApi.AiResponse>() {
            @Override
            public void onResponse(Call<AiRecommendationApi.AiResponse> call, Response<AiRecommendationApi.AiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().recommendations != null) {
                    List<Song> recommendedSongs = new ArrayList<>();
                    for (AiRecommendationApi.AiRecommendation rec : response.body().recommendations) {
                        for (Song song : cachedSongs) {
                            // Sửa lại logic so sánh Tên bài hát cho chắc chắn khớp (Bỏ qua hoa thường và trim)
                            if (song.getTitle() != null && song.getTitle().trim().equalsIgnoreCase(rec.title.trim())) {
                                Song recSong = new Song(song.getSongId(), song.getTitle(), rec.artist != null ? rec.artist : song.getArtist(), song.getAudioUrl());
                                recSong.setConfidence(rec.probability);
                                recommendedSongs.add(recSong);
                                break;
                            }
                        }
                    }
                    if (!recommendedSongs.isEmpty()) {
                        callback.onSuccess(recommendedSongs);
                    } else {
                        // In log nếu AI trả về nhưng lại không khớp được với bài nào trong DB Supabase
                        Log.e("SupabaseModel", "AI trả về nhưng không khớp bài nào trong Database!");
                        callback.onSuccess(getRandomSongs(cachedSongs, 5));
                    }
                } else {
                    Log.e("SupabaseModel", "Lỗi từ API Python: " + response.code());
                    callback.onSuccess(getRandomSongs(cachedSongs, 5));
                }
            }

            @Override
            public void onFailure(Call<AiRecommendationApi.AiResponse> call, Throwable t) {
                Log.e("SupabaseModel", "Sập server AI: " + t.getMessage());
                callback.onSuccess(getRandomSongs(cachedSongs, 5));
            }
        });
    }

    private List<Song> getRandomSongs(List<Song> source, int count) {
        List<Song> result = new ArrayList<>();
        if (source == null || source.isEmpty()) return result;
        List<Song> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        int n = Math.min(count, copy.size());
        for (int i = 0; i < n; i++) {
            result.add(copy.get(i));
        }
        return result;
    }

    @Override
    public void searchSongs(String query, DataCallback<List<Song>> callback) {
        if (query == null || query.trim().isEmpty()) {
            getTopTracks(callback);
            return;
        }

        String bearerAuth = "Bearer " + SUPABASE_ANON_KEY;
        String filter = "ilike.*" + query + "*";

        api.searchSongs(SUPABASE_ANON_KEY, bearerAuth, filter).enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi tìm kiếm: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                callback.onError("Mất kết nối mạng");
            }
        });
    }

    @Override
    public void getSongsByArtist(String artistName, DataCallback<List<Song>> callback) {
        if (artistName == null || artistName.trim().isEmpty()) {
            callback.onError("Tên ca sĩ không hợp lệ");
            return;
        }

        String bearerAuth = "Bearer " + SUPABASE_ANON_KEY;
        String filter = "ilike.*" + artistName + "*";

        api.getSongsByArtist(SUPABASE_ANON_KEY, bearerAuth, filter).enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi lấy danh sách ca sĩ: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                callback.onError("Mất kết nối mạng");
            }
        });
    }
}