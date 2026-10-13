package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.adapter.ArtistAdapter;
import com.example.myapplication.adapter.SongAdapter;
import com.example.myapplication.controller.MusicPlayerController;
import com.example.myapplication.controller.MusicPlayerManager;
import com.example.myapplication.model.IRecommendationModel;
import com.example.myapplication.model.SupabaseRecommendationModel;
import com.example.myapplication.model.Song;
import com.example.myapplication.view.IMusicView;
import com.example.myapplication.model.LibraryManager;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.myapplication.model.AiRecommendationApi;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class PlayerActivity extends AppCompatActivity implements IMusicView {

    private static final String LAST_FM_API_KEY = "4a9f5581a9cdf20a699f540ac52a95c9";

    private TextView tvTitle, tvArtist, tvViewCount, tvCurrentTime, tvTotalTime, tabSongs, tabArtists;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnLike, btnRepeat;
    private ImageView btnBack, ivAlbumArt;
    private SeekBar seekBar;
    private RecyclerView rvRecommendations, rvArtistRecommendations;

    private MusicPlayerController controller;
    private LibraryManager libraryManager;
    private boolean isUserSeeking = false;
    private Song currentSong;
    private List<Song> currentRecommendedSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Ánh xạ UI
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvViewCount = findViewById(R.id.tvViewCount);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnLike = findViewById(R.id.btnLike);
        btnRepeat = findViewById(R.id.btnRepeat);
        seekBar = findViewById(R.id.seekBar);
        btnBack = findViewById(R.id.btnBack);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);

        tabSongs = findViewById(R.id.tabSongs);
        tabArtists = findViewById(R.id.tabArtists);
        rvRecommendations = findViewById(R.id.rvRecommendations);
        rvArtistRecommendations = findViewById(R.id.rvArtistRecommendations);

        rvRecommendations.setLayoutManager(new LinearLayoutManager(this));
        rvArtistRecommendations.setLayoutManager(new LinearLayoutManager(this));

        libraryManager = new LibraryManager(this);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Logic chuyển Tab
        tabSongs.setOnClickListener(v -> {
            tabSongs.setTextColor(Color.parseColor("#1DB954"));
            tabArtists.setTextColor(Color.parseColor("#B3B3B3"));
            rvRecommendations.setVisibility(View.VISIBLE);
            rvArtistRecommendations.setVisibility(View.GONE);
        });

        tabArtists.setOnClickListener(v -> {
            tabArtists.setTextColor(Color.parseColor("#1DB954"));
            tabSongs.setTextColor(Color.parseColor("#B3B3B3"));
            rvRecommendations.setVisibility(View.GONE);
            rvArtistRecommendations.setVisibility(View.VISIBLE);

            if (currentSong != null && currentSong.getArtist() != null) {
                String mainArtist = currentSong.getArtist().split(",")[0].trim();

                Retrofit aiRetrofit = new Retrofit.Builder()
                        .baseUrl("http://189.189.20.101:8080/") // IP mạng LAN
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                AiRecommendationApi aiApi = aiRetrofit.create(AiRecommendationApi.class);

                aiApi.getRelatedArtistsFromAI(mainArtist).enqueue(new Callback<AiRecommendationApi.AiArtistResponse>() {
                    @Override
                    public void onResponse(Call<AiRecommendationApi.AiArtistResponse> call, Response<AiRecommendationApi.AiArtistResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().relatedArtists != null) {
                            
                            // TRUYỀN THẲNG DANH SÁCH TỪ API SANG ADAPTER
                            ArtistAdapter artistAdapter = new ArtistAdapter(response.body().relatedArtists, artistName -> {
                                Intent intent = new Intent(PlayerActivity.this, ArtistProfileActivity.class);
                                intent.putExtra("ARTIST_NAME", artistName);
                                startActivity(intent);
                            });
                            rvArtistRecommendations.setAdapter(artistAdapter);
                        } else {
                            fallbackToSongArtists();
                        }
                    }

                    @Override
                    public void onFailure(Call<AiRecommendationApi.AiArtistResponse> call, Throwable t) {
                        fallbackToSongArtists();
                    }
                });
            } else {
                fallbackToSongArtists();
            }
        });

        btnLike.setOnClickListener(v -> {
            if (currentSong != null) {
                boolean isNowLiked = libraryManager.toggleLike(currentSong);
                updateLikeIcon(isNowLiked);
                if (isNowLiked) {
                    Toast.makeText(this, "Đã thêm vào Thư viện", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đã xóa khỏi Thư viện", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // DÙNG SUPABASE VÀ EXOPLAYER
        controller = MusicPlayerManager.getInstance(this).getController();
        controller.registerView(this);

        btnNext.setOnClickListener(v -> controller.playNextSong());
        btnPrev.setOnClickListener(v -> controller.playPreviousSong());
        btnPlayPause.setOnClickListener(v -> controller.togglePlayPause());
        btnRepeat.setOnClickListener(v -> {
            boolean isRepeat = controller.toggleRepeat();
            if (isRepeat) {
                btnRepeat.setImageResource(R.drawable.ic_repeat_one);
                Toast.makeText(this, "Lặp lại bài hát hiện tại", Toast.LENGTH_SHORT).show();
            } else {
                btnRepeat.setImageResource(R.drawable.ic_repeat);
                Toast.makeText(this, "Phát ngẫu nhiên bài tiếp theo", Toast.LENGTH_SHORT).show();
            }
        });
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                controller.seekTo(seekBar.getProgress());
            }
        });

        // Xử lý nhạc
        boolean openFromMini = getIntent().getBooleanExtra("OPEN_FROM_MINI", false);
        Song initialSong = (Song) getIntent().getSerializableExtra("SONG");
        
        if (initialSong != null && !openFromMini) {
            controller.playSong(initialSong); 
        } else if (controller.getCurrentSong() != null) {
            // Nếu mở từ mini player, load lại thông tin hiện tại
            displaySongInfo(controller.getCurrentSong());
            updatePlayState(controller.isPlaying());
        }
    }

    @Override
    public void displaySongInfo(Song song) {
        currentSong = song;
        runOnUiThread(() -> {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            
            // Format lượt view (vd: 12000 -> 12,000)
            String formattedViews = NumberFormat.getNumberInstance(Locale.US).format(song.getViewCount());
            tvViewCount.setText(formattedViews + " lượt nghe");
            
            updateLikeIcon(libraryManager.isLiked(song));

            // Logic lấy ảnh CỰC TỐI ƯU: 
            // Nếu Supabase có link ảnh (song.getImageUrl() != null) thì tải bằng Glide.
            // Nếu chưa có (hoặc lỗi), dùng cơ chế tạo ảnh Avatar UI dự phòng tuyệt đẹp.
            String imageUrl = song.getImageUrl();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imageUrl = "https://ui-avatars.com/api/?name=" + song.getTitle() + "&background=random&color=fff&size=512&font-size=0.4&bold=true";
            }
            
            Glide.with(this)
                 .load(imageUrl)
                 .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(32)))
                 .placeholder(R.drawable.bg_album_placeholder)
                 .error(R.drawable.bg_album_placeholder)
                 .into(ivAlbumArt);

            // Cập nhật lại tab Ca sĩ nếu đang mở
            if (rvArtistRecommendations.getVisibility() == View.VISIBLE) {
                tabArtists.performClick();
            }
        });
    }

    private void updateLikeIcon(boolean isLiked) {
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_favorite);
        } else {
            btnLike.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public void updatePlayState(boolean isPlaying) {
        runOnUiThread(() -> {
            if (isPlaying) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play);
            }
        });
    }

    @Override
    public void updateProgress(int currentPosition, int totalDuration) {
        runOnUiThread(() -> {
            if (!isUserSeeking) {
                seekBar.setMax(totalDuration);
                seekBar.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition));
                tvTotalTime.setText(formatTime(totalDuration));
            }
        });
    }

    @Override
    public void displayRecommendations(List<Song> recommendedSongs) {
        runOnUiThread(() -> {
            if (recommendedSongs != null && !recommendedSongs.isEmpty()) {
                currentRecommendedSongs = recommendedSongs;
                if (rvRecommendations.getAdapter() instanceof SongAdapter) {
                    ((SongAdapter) rvRecommendations.getAdapter()).updateData(recommendedSongs);
                } else {
                    SongAdapter adapter = new SongAdapter(recommendedSongs, true, song -> {
                        controller.playSong(song);
                    });
                    rvRecommendations.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void showError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            // Chỉ gỡ View hiện tại ra khỏi Controller để không bị rò rỉ bộ nhớ.
            // TUYỆT ĐỐI KHÔNG GỌI release() ở đây vì nhạc cần tiếp tục phát ngầm cho Mini Player
            controller.unregisterView(this);
        }
    }

    private String formatTime(int ms) {
        int seconds = (ms / 1000) % 60;
        int minutes = (ms / 1000) / 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void fallbackToSongArtists() {
        List<AiRecommendationApi.AiArtistRecommendation> artists = new ArrayList<>();
        List<String> seenNames = new ArrayList<>();
        
        for (Song s : currentRecommendedSongs) {
            if (s.getArtist() != null) {
                String[] splitArtists = s.getArtist().split(",");
                for (String artist : splitArtists) {
                    String cleanArtistName = artist.trim();
                    if (!cleanArtistName.isEmpty() && !seenNames.contains(cleanArtistName) && !cleanArtistName.equalsIgnoreCase("Unknown Artist")) {
                        seenNames.add(cleanArtistName);
                        
                        // Tạo object dự phòng không có điểm
                        AiRecommendationApi.AiArtistRecommendation fallbackRec = new AiRecommendationApi.AiArtistRecommendation();
                        fallbackRec.artist = cleanArtistName;
                        fallbackRec.score = 0; // Để 0 thì Adapter tự hiểu là không có điểm
                        artists.add(fallbackRec);
                    }
                }
            }
        }
        
        ArtistAdapter artistAdapter = new ArtistAdapter(artists, artistName -> {
            Intent intent = new Intent(PlayerActivity.this, ArtistProfileActivity.class);
            intent.putExtra("ARTIST_NAME", artistName);
            startActivity(intent);
        });
        rvArtistRecommendations.setAdapter(artistAdapter);
    }
}