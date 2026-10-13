package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.SongAdapter;
import com.example.myapplication.controller.MusicPlayerManager;
import com.example.myapplication.model.IRecommendationModel;
import com.example.myapplication.model.Song;
import com.example.myapplication.model.SupabaseRecommendationModel;

import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView rvSearchSongs;
    private EditText edtSearch;
    private SongAdapter songAdapter;
    private IRecommendationModel model;

    // Tối ưu hóa Tìm kiếm: Bộ đếm thời gian (Debouncer) chống Spam API
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rvSearchSongs = view.findViewById(R.id.rvSearchSongs);
        edtSearch = view.findViewById(R.id.edtSearch);

        // Tối ưu hóa RecyclerView: Khóa kích thước + Tạo bộ nhớ đệm
        rvSearchSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchSongs.setHasFixedSize(true);
        rvSearchSongs.setItemViewCacheSize(20);

        model = new SupabaseRecommendationModel();
        loadTopTracks();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Hủy lệnh tìm kiếm cũ nếu người dùng vẫn đang gõ chữ
                searchHandler.removeCallbacks(searchRunnable);

                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        loadTopTracks();
                    } else {
                        searchSongs(query);
                    }
                };

                // Chỉ gọi API nếu người dùng DỪNG GÕ QUÁ 500ms (Nửa giây)
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });

        return view;
    }

    private void loadTopTracks() {
        model.getTopTracks(new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> setupAdapter(songs));
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

    private void searchSongs(String query) {
        model.searchSongs(query, new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> setupAdapter(songs));
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
        // Tối ưu hóa: Chỉ tạo Adapter 1 lần, các lần sau chỉ update Data
        if (songAdapter == null) {
            songAdapter = new SongAdapter(songs, song -> playSongViaMainActivity(song));
            rvSearchSongs.setAdapter(songAdapter);
        } else {
            songAdapter.updateData(songs);
        }
    }

    private void playSongViaMainActivity(Song song) {
        MusicPlayerManager.getInstance(getContext()).getController().playSong(song);
    }
}