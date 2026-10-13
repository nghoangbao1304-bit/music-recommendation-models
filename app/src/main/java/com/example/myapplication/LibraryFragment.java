package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.SongAdapter;
import com.example.myapplication.controller.MusicPlayerController;
import com.example.myapplication.controller.MusicPlayerManager;
import com.example.myapplication.model.LibraryManager;
import com.example.myapplication.model.Song;

import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView rvLibrarySongs;
    private TextView tvEmpty;
    private SongAdapter adapter;
    private LibraryManager libraryManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        rvLibrarySongs = view.findViewById(R.id.rvLibrarySongs);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        libraryManager = new LibraryManager(getContext());
        rvLibrarySongs.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onResume() {
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
                playSongViaMainActivity(song);
            });
            rvLibrarySongs.setAdapter(adapter);
        }
    }

    private void playSongViaMainActivity(Song song) {
        MusicPlayerManager.getInstance(getContext()).getController().playSong(song);
    }
}