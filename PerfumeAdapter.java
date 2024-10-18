package com.example.perfume;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PerfumeAdapter extends RecyclerView.Adapter<PerfumeAdapter.ViewHolder> {
    private final List<Perfume> perfumeList;

    public PerfumeAdapter(List<Perfume> perfumeList) {
        this.perfumeList = perfumeList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView perfumeName;
        public TextView perfumeNotes;
        public ImageView perfumeImage;

        public ViewHolder(View view) {
            super(view);
            perfumeName = view.findViewById(R.id.perfume_name);
            perfumeNotes = view.findViewById(R.id.perfume_notes);
            perfumeImage = view.findViewById(R.id.perfume_image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.perfume_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Perfume perfume = perfumeList.get(position);
        holder.perfumeName.setText(perfume.getName());
        holder.perfumeNotes.setText(perfume.getNotes());

        // 이미지 로드 (Glide 사용)
        Glide.with(holder.itemView.getContext())
                .load(perfume.getImageUrl())
                .into(holder.perfumeImage);
    }

    @Override
    public int getItemCount() {
        return perfumeList.size();
    }
}
