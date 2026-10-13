package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.model.Song;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songList;
    private OnItemClickListener listener;
    private boolean showConfidence = false;

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public void updateData(List<Song> newSongs) {
        this.songList = newSongs;
        notifyDataSetChanged();
    }

    public SongAdapter(List<Song> songList, OnItemClickListener listener) {
        this(songList, false, listener);
    }

    public SongAdapter(List<Song> songList, boolean showConfidence, OnItemClickListener listener) {
        this.songList = songList;
        this.showConfidence = showConfidence;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        
        // Hiển thị lượt nghe
        String formattedViews = NumberFormat.getNumberInstance(Locale.US).format(song.getViewCount());
        if (holder.tvViewCount != null) {
            holder.tvViewCount.setText(formattedViews + " lượt nghe");
        }
        
        // --- HIỂN THỊ ẢNH BÌA BẰNG GLIDE ---
        String imageUrl = song.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "https://ui-avatars.com/api/?name=" + song.getTitle() + "&background=random&color=fff&size=512&font-size=0.4&bold=true";
        }
        
        Glide.with(holder.itemView.getContext())
             .load(imageUrl)
             .diskCacheStrategy(DiskCacheStrategy.ALL)
             .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(16)))
             .placeholder(R.drawable.bg_album_placeholder)
             .error(R.drawable.bg_album_placeholder)
             .into(holder.ivAlbum);

        // Hiển thị điểm confidence (độ tin cậy) nếu được bật và AI có trả về > 0
        if (showConfidence && song.getConfidence() > 0) {
            holder.tvConfidence.setVisibility(View.VISIBLE);
            int percent = (int) (song.getConfidence() * 100);
            holder.tvConfidence.setText("AI Trùng khớp: " + percent + "%");
        } else {
            holder.tvConfidence.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(song));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvConfidence, tvViewCount;
        ImageView ivAlbum;
        
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvArtist = itemView.findViewById(R.id.tvItemArtist);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            tvViewCount = itemView.findViewById(R.id.tvItemViewCount);
            // Bạn cần đảm bảo trong item_song.xml có ImageView id là ivItemAlbumArt
            ivAlbum = itemView.findViewById(R.id.ivItemAlbumArt);
        }
    }
}