package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AiRecommendationApi {
    
    class AiRecommendation {
        @SerializedName("title")
        public String title;
        @SerializedName("artist")
        public String artist;
        @SerializedName("probability")
        public double probability;
    }

    class AiResponse {
        @SerializedName("current_song_title")
        public String currentSongTitle;
        @SerializedName("recommendations")
        public List<AiRecommendation> recommendations;
    }

    class AiArtistRecommendation {
        @SerializedName("artist")
        public String artist;
        @SerializedName("score")
        public double score;
    }

    class AiArtistResponse {
        @SerializedName("current_artist")
        public String currentArtist;
        @SerializedName("related_artists")
        public List<AiArtistRecommendation> relatedArtists;
    }

    @GET("recommend/{song_title}")
    Call<AiResponse> getNextSongFromAI(@Path("song_title") String currentSongTitle);

    @GET("recommend_artist/{artist_name}")
    Call<AiArtistResponse> getRelatedArtistsFromAI(@Path("artist_name") String artistName);
}