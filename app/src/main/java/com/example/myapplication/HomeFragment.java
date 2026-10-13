package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RecentSongAdapter;
import com.example.myapplication.adapter.SongAdapter;
import com.example.myapplication.controller.MusicPlayerManager;
import com.example.myapplication.model.IRecommendationModel;
import com.example.myapplication.model.LibraryManager;
import com.example.myapplication.model.Song;
import com.example.myapplication.model.SupabaseRecommendationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvSongs, rvRecentSongs, rvTopViews;
    private TextView tvRecentHeader;
    private SongAdapter songAdapter;
    private RecentSongAdapter topViewsAdapter;
    private IRecommendationModel model;
    private LibraryManager libraryManager;

    // Lưu danh sách Khám phá hôm nay cố định trong phiên làm việc
    private static List<Song> cachedDiscoverSongs;

    public static void clearDiscoverCache() {
        cachedDiscoverSongs = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvSongs = view.findViewById(R.id.rvSongs);
        rvRecentSongs = view.findViewById(R.id.rvRecentSongs);
        rvTopViews = view.findViewById(R.id.rvTopViews);
        tvRecentHeader = view.findViewById(R.id.tvRecentHeader);

        // Khám phá (Dọc)
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSongs.setHasFixedSize(true);
        rvSongs.setItemViewCacheSize(20);

        // Nghe gần đây (Ngang)
        rvRecentSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecentSongs.setHasFixedSize(true);

        // Thịnh hành (Ngang)
        rvTopViews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopViews.setHasFixedSize(true);
        rvTopViews.setItemViewCacheSize(10);

        libraryManager = new LibraryManager(getContext());
        model = new SupabaseRecommendationModel();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentSongs();
        loadTopTracks();
    }

    public void loadRecentSongs() {
        if (getContext() == null || libraryManager == null) return;
        List<Song> recentSongs = libraryManager.getRecentSongs();
        if (recentSongs != null && !recentSongs.isEmpty()) {
            tvRecentHeader.setVisibility(View.VISIBLE);
            rvRecentSongs.setVisibility(View.VISIBLE);

            if (rvRecentSongs.getAdapter() instanceof RecentSongAdapter) {
                ((RecentSongAdapter) rvRecentSongs.getAdapter()).updateData(recentSongs);
            } else {
                RecentSongAdapter adapter = new RecentSongAdapter(recentSongs, song -> playSongViaMainActivity(song));
                rvRecentSongs.setAdapter(adapter);
            }
        } else {
            tvRecentHeader.setVisibility(View.GONE);
            rvRecentSongs.setVisibility(View.GONE);
        }
    }

    private void loadTopTracks() {
        model.getTopTracks(new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                if (getActivity() != null && songs != null && !songs.isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        // 1. Lấy Top 10 bài nhiều View nhất làm Thịnh Hành
                        int topCount = Math.min(10, songs.size());
                        List<Song> top10 = new ArrayList<>(songs.subList(0, topCount));

                        if (topViewsAdapter == null) {
                            topViewsAdapter = new RecentSongAdapter(top10, song -> playSongViaMainActivity(song));
                            rvTopViews.setAdapter(topViewsAdapter);
                        } else {
                            topViewsAdapter.updateData(top10);
                        }

                        // 2. Mục Khám phá hôm nay: Chỉ random LẦN ĐẦU TIÊN khi đăng nhập
                        if (cachedDiscoverSongs == null || cachedDiscoverSongs.isEmpty()) {
                            List<Song> discoverList = new ArrayList<>(songs);
                            Collections.shuffle(discoverList);
                            int discoverCount = Math.min(20, discoverList.size());
                            cachedDiscoverSongs = new ArrayList<>(discoverList.subList(0, discoverCount));
                        } else {
                            // Cập nhật lại lượt xem mới nếu có nhưng giữ nguyên thứ tự danh sách
                            for (Song cachedSong : cachedDiscoverSongs) {
                                for (Song updatedSong : songs) {
                                    if (cachedSong.getSongId() != null && cachedSong.getSongId().equals(updatedSong.getSongId())) {
                                        cachedSong.setViewCount(updatedSong.getViewCount());
                                        break;
                                    }
                                }
                            }
                        }

                        setupAdapter(cachedDiscoverSongs);
                    });
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void setupAdapter(List<Song> songs) {
        if (songAdapter == null) {
            songAdapter = new SongAdapter(songs, song -> playSongViaMainActivity(song));
            rvSongs.setAdapter(songAdapter);
        } else {
            songAdapter.updateData(songs);
        }
    }

    private void playSongViaMainActivity(Song song) {
        MusicPlayerManager.getInstance(getContext()).getController().playSong(song);
    }
}