package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.SongAdapter;
import com.example.myapplication.model.IRecommendationModel;
import com.example.myapplication.model.SupabaseRecommendationModel;
import com.example.myapplication.model.Song;

import java.util.List;

import android.view.View;
import android.widget.TextView;
import com.example.myapplication.adapter.RecentSongAdapter;
import com.example.myapplication.model.LibraryManager;

public class HomeActivity extends AppCompatActivity {
    
    private RecyclerView rvSongs, rvRecentSongs;
    private TextView tvRecentHeader;
    private EditText edtSearch;
    private ImageButton btnSearch, btnLibrary;
    private SongAdapter songAdapter;
    private IRecommendationModel model;
    private LibraryManager libraryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvSongs = findViewById(R.id.rvSongs);
        rvRecentSongs = findViewById(R.id.rvRecentSongs);
        tvRecentHeader = findViewById(R.id.tvRecentHeader);
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnLibrary = findViewById(R.id.btnLibrary);

        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvRecentSongs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        libraryManager = new LibraryManager(this);
        model = new SupabaseRecommendationModel(); 

        loadTopTracks();

        // Xử lý nút Tìm kiếm
        btnSearch.setOnClickListener(v -> {
            String query = edtSearch.getText().toString().trim();
            searchSongs(query);
        });

        // Hỗ trợ tìm kiếm nhanh khi gõ (Real-time search)
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadTopTracks();
                } else {
                    searchSongs(query);
                }
            }
        });

        // Xử lý nút mở Thư viện
        btnLibrary.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LibraryActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentSongs();
    }

    private void loadRecentSongs() {
        List<Song> recentSongs = libraryManager.getRecentSongs();
        if (recentSongs != null && !recentSongs.isEmpty()) {
            tvRecentHeader.setVisibility(View.VISIBLE);
            rvRecentSongs.setVisibility(View.VISIBLE);
            
            RecentSongAdapter adapter = new RecentSongAdapter(recentSongs, song -> {
                Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
                intent.putExtra("SONG", song);
                startActivity(intent);
            });
            rvRecentSongs.setAdapter(adapter);
        } else {
            tvRecentHeader.setVisibility(View.GONE);
            rvRecentSongs.setVisibility(View.GONE);
        }
    }

    private void loadTopTracks() {
        model.getTopTracks(new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                runOnUiThread(() -> setupAdapter(songs));
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, errorMsg, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void searchSongs(String query) {
        model.searchSongs(query, new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                runOnUiThread(() -> setupAdapter(songs));
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, errorMsg, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void setupAdapter(List<Song> songs) {
        songAdapter = new SongAdapter(songs, song -> {
            Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
            intent.putExtra("SONG", song);
            startActivity(intent);
        });
        rvSongs.setAdapter(songAdapter);
    }
}