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
import com.example.myapplication.model.SupabaseRecommendationModel;
import com.example.myapplication.view.IMusicView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicPlayerController {
    private ExoPlayer exoPlayer;
    private final IRecommendationModel recommendationModel;
    private final List<IMusicView> views = new CopyOnWriteArrayList<>();
    private Song currentSong;
    private List<Song> currentRecommendations;
    private final Context context;

    private boolean isPrepared = false;
    private boolean isRepeatOne = false; // Biến trạng thái lặp bài
    private LibraryManager libraryManager;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer != null && exoPlayer.isPlaying() && !views.isEmpty()) {
                notifyProgress((int) exoPlayer.getCurrentPosition(), (int) exoPlayer.getDuration());
                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    public MusicPlayerController(Context context, IRecommendationModel recommendationModel, IMusicView view) {
        this.context = context;
        this.recommendationModel = recommendationModel;
        if (view != null) {
            registerView(view);
        }
        this.libraryManager = new LibraryManager(context);
        initMediaPlayer();
    }

    public void registerView(IMusicView view) {
        if (view != null && !views.contains(view)) {
            views.add(view);
            // Bắn lại data ngay khi có view mới attach
            if (currentSong != null) {
                view.displaySongInfo(currentSong);
                if (exoPlayer != null) {
                    view.updatePlayState(exoPlayer.isPlaying());
                    view.updateProgress((int) exoPlayer.getCurrentPosition(), (int) exoPlayer.getDuration());
                }
            }
            if (currentRecommendations != null && !currentRecommendations.isEmpty()) {
                view.displayRecommendations(currentRecommendations);
            } else if (currentSong != null) {
                fetchRecommendationsFromAI(currentSong.getTitle());
            }
        }
    }

    public void unregisterView(IMusicView view) {
        if (view != null) {
            views.remove(view);
        }
    }

    public void setView(IMusicView view) {
        if (view != null) {
            registerView(view);
        }
    }

    private void notifyDisplaySongInfo(Song song) {
        for (IMusicView v : views) {
            v.displaySongInfo(song);
        }
    }

    private void notifyPlayState(boolean isPlaying) {
        for (IMusicView v : views) {
            v.updatePlayState(isPlaying);
        }
    }

    private void notifyProgress(int currentPosition, int totalDuration) {
        for (IMusicView v : views) {
            v.updateProgress(currentPosition, totalDuration);
        }
    }

    private void notifyRecommendations(List<Song> songs) {
        for (IMusicView v : views) {
            v.displayRecommendations(songs);
        }
    }

    private void notifyError(String errorMsg) {
        for (IMusicView v : views) {
            v.showError(errorMsg);
        }
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    private void initMediaPlayer() {
        // Khởi tạo ExoPlayer - Bộ giải mã âm thanh tốt nhất của Android
        exoPlayer = new ExoPlayer.Builder(context).build();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    isPrepared = true;
                    notifyPlayState(exoPlayer.getPlayWhenReady());
                    if (exoPlayer.getPlayWhenReady()) {
                        startProgressUpdate();
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    if (isRepeatOne) {
                        exoPlayer.seekTo(0);
                        exoPlayer.play();
                    } else {
                        stopProgressUpdate();
                        playNextSong();
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                notifyPlayState(isPlaying);
                if (isPlaying) {
                    startProgressUpdate();
                } else {
                    stopProgressUpdate();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                isPrepared = false;
                notifyError("Lỗi kết nối âm thanh!");
            }
        });
    }

    // Lịch sử phát nhạc để hỗ trợ nút Previous / Next
    private final List<Song> songHistory = new ArrayList<>();
    private int historyIndex = -1;

    public void playNextSong() {
        stopProgressUpdate();
        
        // Nếu còn bài tiếp theo trong lịch sử (do người dùng từng bấm Previous)
        if (historyIndex >= 0 && historyIndex < songHistory.size() - 1) {
            historyIndex++;
            Song nextSong = songHistory.get(historyIndex);
            playSongInternal(nextSong);
            return;
        }

        // Nếu hết lịch sử -> gọi AI lấy bài gợi ý tiếp theo
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
                notifyError(errorMsg);
            }
        });
    }

    public void playPreviousSong() {
        if (exoPlayer == null) return;

        // Nếu có bài trước đó trong lịch sử phát -> lập tức quay lại bài trước
        if (historyIndex > 0) {
            historyIndex--;
            Song prevSong = songHistory.get(historyIndex);
            playSongInternal(prevSong);
        } else {
            // Nếu không có bài trước -> tua lại từ đầu bài hiện tại
            exoPlayer.seekTo(0);
            if (!exoPlayer.isPlaying()) {
                exoPlayer.play();
            }
        }
    }

    public void playSong(Song song) {
        if (song == null) return;

        // Nếu bài mới được bấm chọn từ danh sách, lưu vào history
        if (historyIndex < 0 || historyIndex >= songHistory.size() || !isSameSong(songHistory.get(historyIndex), song)) {
            // Nếu người dùng đang ở giữa history mà chọn bài mới, xóa các bài đằng sau
            if (historyIndex >= 0 && historyIndex < songHistory.size() - 1) {
                songHistory.subList(historyIndex + 1, songHistory.size()).clear();
            }
            songHistory.add(song);
            historyIndex = songHistory.size() - 1;
        }

        playSongInternal(song);
    }

    private boolean isSameSong(Song s1, Song s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.getSongId() != null && s2.getSongId() != null) {
            return s1.getSongId().equals(s2.getSongId());
        }
        if (s1.getTitle() != null && s2.getTitle() != null) {
            return s1.getTitle().equalsIgnoreCase(s2.getTitle());
        }
        return false;
    }

    private void playSongInternal(Song song) {
        try {
            isPrepared = false;
            currentRecommendations = null; // Clear old recommendations for new song
            
            // 1. TĂNG VIEW NGAY LẬP TỨC (TRÊN RAM) ĐỂ HIỂN THỊ
            int currentViews = song.getViewCount();
            song.setViewCount(currentViews + 1);
            
            this.currentSong = song;

            // 2. LƯU BÀI HÁT ĐÃ ĐƯỢC TĂNG VIEW NÀY VÀO LỊCH SỬ NGHE GẦN ĐÂY
            if (libraryManager != null) {
                libraryManager.addRecentSong(song);
            }
            
            // 3. GỬI LÊN SUPABASE LƯỢT VIEW MỚI ĐỂ CẬP NHẬT TRÊN MẠNG
            if (recommendationModel instanceof SupabaseRecommendationModel) {
                ((SupabaseRecommendationModel) recommendationModel).incrementSongView(song);
            }
            
            String url = song.getAudioUrl();
            if (url == null || url.trim().isEmpty()) {
                notifyError("Bài hát này không có link âm thanh!");
                return;
            }
            
            url = url.trim();
            if (url.startsWith("http://")) {
                url = url.replace("http://", "https://");
            }
            
            Log.d("MusicController", "Đang phát URL: " + url);
            
            MediaItem mediaItem = MediaItem.fromUri(url);
            exoPlayer.setMediaItem(mediaItem);
            
            notifyDisplaySongInfo(song); // Render giao diện cho tất cả view
            
            exoPlayer.prepare();
            exoPlayer.play();
            
            // GỌI AI ĐỂ LẤY TOP 5 GỢI Ý CHO BÀI NÀY HIỂN THỊ LÊN MÀN HÌNH
            fetchRecommendationsFromAI(song.getTitle());
            
        } catch (Exception e) {
            e.printStackTrace();
            notifyError("Lỗi kết nối âm thanh: " + e.getMessage());
        }
    }

    private void fetchRecommendationsFromAI(String title) {
        recommendationModel.getRecommendations(title, new IRecommendationModel.DataCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songs) {
                currentRecommendations = songs;
                notifyRecommendations(songs);
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