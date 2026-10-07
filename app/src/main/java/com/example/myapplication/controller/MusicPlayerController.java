package com.example.myapplication.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.myapplication.model.IRecommendationModel;
import com.example.myapplication.model.LibraryManager;
import com.example.myapplication.model.Song;
import com.example.myapplication.view.IMusicView;

import java.util.List;

public class MusicPlayerController {
    private ExoPlayer exoPlayer;
    private final IRecommendationModel recommendationModel;
    private final IMusicView view;
    private Song currentSong;
    private final Context context;

    private boolean isPrepared = false;
    private boolean isRepeatOne = false; // Biến trạng thái lặp bài
    private LibraryManager libraryManager;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                view.updateProgress((int) exoPlayer.getCurrentPosition(), (int) exoPlayer.getDuration());
                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    public MusicPlayerController(Context context, IRecommendationModel recommendationModel, IMusicView view) {
        this.context = context;
        this.recommendationModel = recommendationModel;
        this.view = view;
        this.libraryManager = new LibraryManager(context);
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        // Khởi tạo ExoPlayer - Bộ giải mã âm thanh tốt nhất của Android
        exoPlayer = new ExoPlayer.Builder(context).build();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    isPrepared = true;
                    view.updatePlayState(exoPlayer.getPlayWhenReady());
                    if (exoPlayer.getPlayWhenReady()) {
                        startProgressUpdate();
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    if (isRepeatOne) {
                        // Lặp lại bài hát hiện tại
                        exoPlayer.seekTo(0);
                        exoPlayer.play();
                    } else {
                        Log.d("MusicController", "Song completed. Fetching next...");
                        stopProgressUpdate();
                        playNextSong();
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                view.updatePlayState(isPlaying);
                if (isPlaying) {
                    startProgressUpdate();
                } else {
                    stopProgressUpdate();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e("MusicController", "ExoPlayer Error: " + error.getMessage());
                isPrepared = false;
                view.showError("Lỗi kết nối âm thanh!");
            }
        });
    }

    public void playNextSong() {
        stopProgressUpdate();
        // Truyền TÊN BÀI HÁT (Title) thay vì ID
        String currentTitle = (currentSong != null) ? currentSong.getTitle() : null;
        
        recommendationModel.getNextSong(currentTitle, new IRecommendationModel.DataCallback<Song>() {
            @Override
            public void onSuccess(Song song) {
                if (song != null) {
                    playSong(song);
                }
            }

            @Override
            public void onError(String errorMsg) {
                view.showError(errorMsg);
            }
        });
    }

    public void playPreviousSong() {
        if (exoPlayer != null) {
            exoPlayer.seekTo(0);
            if (!exoPlayer.isPlaying()) {
                exoPlayer.play();
            }
        }
    }

    public void playSong(Song song) {
        this.currentSong = song;
        
        // Thêm vào lịch sử nghe gần đây
        if (libraryManager != null) {
            libraryManager.addRecentSong(song);
        }
        
        try {
            isPrepared = false;
            
            String url = song.getAudioUrl();
            if (url == null || url.trim().isEmpty()) {
                view.showError("Bài hát này không có link âm thanh!");
                return;
            }
            
            url = url.trim();
            if (url.startsWith("http://")) {
                url = url.replace("http://", "https://");
            }
            
            Log.d("MusicController", "Đang phát URL: " + url);
            
            MediaItem mediaItem = MediaItem.fromUri(url);
            exoPlayer.setMediaItem(mediaItem);
            view.displaySongInfo(song);
            
            exoPlayer.prepare();
            exoPlayer.play();
            
            // GỌI AI ĐỂ LẤY TOP 5 GỢI Ý CHO BÀI NÀY HIỂN THỊ LÊN MÀN HÌNH
            fetchRecommendationsFromAI(song.getTitle());
            
        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Lỗi kết nối âm thanh: " + e.getMessage());
        }
    }

    private void fetchRecommendationsFromAI(String title) {
        recommendationModel.getRecommendations(title, new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                view.displayRecommendations(songs);
            }

            @Override
            public void onError(String errorMsg) {
                // Chỉ log ra, không báo lỗi chướng mắt người dùng
                Log.e("MusicController", "Không lấy được gợi ý: " + errorMsg);
            }
        });
    }

    public void togglePlayPause() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                exoPlayer.pause();
            } else if (isPrepared) {
                exoPlayer.play();
            }
        }
    }

    public void seekTo(int position) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(position);
        }
    }

    private void startProgressUpdate() {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdate() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    public boolean toggleRepeat() {
        isRepeatOne = !isRepeatOne;
        return isRepeatOne;
    }

    public void release() {
        stopProgressUpdate();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}