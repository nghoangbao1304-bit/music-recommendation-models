package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.controller.MusicPlayerController;
import com.example.myapplication.controller.MusicPlayerManager;
import com.example.myapplication.model.Song;
import com.example.myapplication.model.SupabaseRecommendationModel;
import com.example.myapplication.view.IMusicView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements IMusicView {

    private BottomNavigationView bottomNav;
    private ConstraintLayout layoutMiniPlayer;
    private ImageView ivMiniAlbum;
    private TextView tvMiniTitle, tvMiniArtist;
    private ImageButton btnMiniPlayPause, btnMiniPrev, btnMiniNext;
    private ProgressBar progressMini;

    private MusicPlayerController playerController;
    private Song currentSong;

    @Override
    protected void onResume() {
        super.onResume();
        if (playerController != null) {
            // Khi người dùng bấm Back từ PlayerActivity về Trang chủ
            // Phải gắn lại View vào MainActivity để MiniPlayer tự động lấy thông tin phát nhạc hiện tại
            playerController.registerView(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ UI
        bottomNav = findViewById(R.id.bottom_navigation);
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer);
        ivMiniAlbum = findViewById(R.id.ivMiniAlbum);
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        tvMiniArtist = findViewById(R.id.tvMiniArtist);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        btnMiniPrev = findViewById(R.id.btnMiniPrev);
        btnMiniNext = findViewById(R.id.btnMiniNext);
        progressMini = findViewById(R.id.progressMini);

        // Khởi tạo Player Controller toàn cục (dùng cho mọi màn hình)
        playerController = MusicPlayerManager.getInstance(this).getController();
        playerController.registerView(this);

        // Setup Bottom Nav
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.nav_library) {
                selectedFragment = new LibraryFragment();
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Chọn tab Home làm mặc định
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // Sự kiện Mini Player
        btnMiniPlayPause.setOnClickListener(v -> playerController.togglePlayPause());
        btnMiniPrev.setOnClickListener(v -> playerController.playPreviousSong());
        btnMiniNext.setOnClickListener(v -> playerController.playNextSong());
        
        layoutMiniPlayer.setOnClickListener(v -> {
            if (currentSong != null) {
                Intent intent = new Intent(this, PlayerActivity.class);
                // Gửi cờ báo hiệu không phát lại từ đầu nếu đã đang phát
                intent.putExtra("OPEN_FROM_MINI", true);
                intent.putExtra("SONG", currentSong);
                startActivity(intent);
            }
        });
    }

    public MusicPlayerController getPlayerController() {
        return playerController;
    }

    // --- IMusicView Implementation ---
    @Override
    public void displaySongInfo(Song song) {
        this.currentSong = song;
        runOnUiThread(() -> {
            layoutMiniPlayer.setVisibility(View.VISIBLE);
            tvMiniTitle.setText(song.getTitle());
            tvMiniArtist.setText(song.getArtist());
            
            String imageUrl = song.getImageUrl();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imageUrl = "https://ui-avatars.com/api/?name=" + song.getTitle() + "&background=random&color=fff&size=512&font-size=0.4&bold=true";
            }
            Glide.with(this).load(imageUrl)
                 .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(8)))
                 .placeholder(R.drawable.bg_album_placeholder)
                 .into(ivMiniAlbum);

            // Cập nhật lại ngay danh mục Nghe gần đây nếu đang hiển thị Trang chủ (HomeFragment)
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).loadRecentSongs();
            }
        });
    }

    @Override
    public void updatePlayState(boolean isPlaying) {
        runOnUiThread(() -> {
            if (isPlaying) {
                btnMiniPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                btnMiniPlayPause.setImageResource(R.drawable.ic_play);
            }
        });
    }

    @Override
    public void updateProgress(int currentPosition, int totalDuration) {
        runOnUiThread(() -> {
            progressMini.setMax(totalDuration);
            progressMini.setProgress(currentPosition);
        });
    }

    @Override
    public void displayRecommendations(List<Song> recommendedSongs) {
        // Không hiển thị ở mini player
    }

    @Override
    public void showError(String errorMessage) {
        runOnUiThread(() -> Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show());
    }

    // Xóa onDestroy release để nó chạy toàn cầu
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerController != null) {
            playerController.unregisterView(this); // Chỉ gỡ view thôi, không tắt nhạc
        }
    }
}