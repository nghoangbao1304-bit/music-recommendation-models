package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.List;
import com.example.myapplication.model.AiRecommendationApi;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {
    private final List<AiRecommendationApi.AiArtistRecommendation> artistList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String artistName);
    }

    public ArtistAdapter(List<AiRecommendationApi.AiArtistRecommendation> artistList, OnItemClickListener listener) {
        this.artistList = artistList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        AiRecommendationApi.AiArtistRecommendation artist = artistList.get(position);
        holder.tvArtistName.setText(artist.artist);
        
        if (artist.score > 0) {
            holder.tvConfidence.setVisibility(View.VISIBLE);
            int percent = (int) (artist.score * 100);
            holder.tvConfidence.setText("AI Gợi ý: " + percent + "%");
        } else {
            holder.tvConfidence.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(artist.artist));
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView tvArtistName, tvConfidence;
        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArtistName = itemView.findViewById(R.id.tvArtistNameItem);
            tvConfidence = itemView.findViewById(R.id.tvArtistConfidence);
        }
    }
}