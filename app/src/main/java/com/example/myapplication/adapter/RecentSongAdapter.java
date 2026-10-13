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
import java.util.List;

public class RecentSongAdapter extends RecyclerView.Adapter<RecentSongAdapter.ViewHolder> {
    private List<Song> songList;
    private final SongAdapter.OnItemClickListener listener;

    public void updateData(List<Song> newSongs) {
        this.songList = newSongs;
        notifyDataSetChanged();
    }

    public RecentSongAdapter(List<Song> songList, SongAdapter.OnItemClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_recent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        
        // --- HIỂN THỊ ẢNH BẰNG GLIDE ---
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

        holder.itemView.setOnClickListener(v -> listener.onItemClick(song));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivAlbum;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecentTitle);
            ivAlbum = itemView.findViewById(R.id.ivRecentAlbumArt);
        }
    }
}