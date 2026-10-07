package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.SongAdapter;
import com.example.myapplication.model.IRecommendationModel;
import com.example.myapplication.model.SupabaseRecommendationModel;
import com.example.myapplication.model.Song;

import java.util.List;

public class ArtistProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvArtistName, tvTopSongsTitle;
    private RecyclerView rvArtistSongs;
    
    private SongAdapter adapter;
    private IRecommendationModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_profile);

        btnBack = findViewById(R.id.btnBack);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvTopSongsTitle = findViewById(R.id.tvTopSongsTitle);
        rvArtistSongs = findViewById(R.id.rvArtistSongs);

        rvArtistSongs.setLayoutManager(new LinearLayoutManager(this));
        model = new SupabaseRecommendationModel();

        btnBack.setOnClickListener(v -> finish());

        // Nhận tên Ca Sĩ từ Intent gửi sang
        String artistName = getIntent().getStringExtra("ARTIST_NAME");
        
        if (artistName != null && !artistName.isEmpty()) {
            tvArtistName.setText(artistName);
            loadArtistSongs(artistName);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy tên Ca sĩ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadArtistSongs(String artistName) {
        model.getSongsByArtist(artistName, new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                runOnUiThread(() -> {
                    if (songs != null && !songs.isEmpty()) {
                        tvTopSongsTitle.setText("Bài hát phổ biến (" + songs.size() + ")");
                        adapter = new SongAdapter(songs, song -> {
                            Intent intent = new Intent(ArtistProfileActivity.this, PlayerActivity.class);
                            intent.putExtra("SONG", song);
                            startActivity(intent);
                        });
                        rvArtistSongs.setAdapter(adapter);
                    } else {
                        tvTopSongsTitle.setText("Chưa có bài hát nào");
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> Toast.makeText(ArtistProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show());
            }
        });
    }
}