package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.SongAdapter;
import com.example.myapplication.model.LibraryManager;
import com.example.myapplication.model.Song;

import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    private RecyclerView rvLibrarySongs;
    private TextView tvEmpty;
    private ImageView btnBack;
    private SongAdapter adapter;
    private LibraryManager libraryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        rvLibrarySongs = findViewById(R.id.rvLibrarySongs);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        libraryManager = new LibraryManager(this);
        rvLibrarySongs.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLibrary();
    }

    private void loadLibrary() {
        List<Song> likedSongs = libraryManager.getLikedSongs();

        if (likedSongs.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvLibrarySongs.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvLibrarySongs.setVisibility(View.VISIBLE);

            adapter = new SongAdapter(likedSongs, song -> {
                Intent intent = new Intent(LibraryActivity.this, PlayerActivity.class);
                intent.putExtra("SONG", song);
                startActivity(intent);
            });
            rvLibrarySongs.setAdapter(adapter);
        }
    }
}