package com.example.calculator;

import android.content.Context;
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

    private List<Perfume> perfumes;
    private Context context;

    public PerfumeAdapter(List<Perfume> perfumes, Context context) {
        this.perfumes = perfumes;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.perfume_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Perfume perfume = perfumes.get(position);
        holder.perfumeName.setText(perfume.getName());
        holder.perfumeNotes.setText(perfume.getNotes());

        // 이미지 로드 (Glide 사용)
        Glide.with(context).load(perfume.getImageURL()).into(holder.perfumeImage);
    }

    @Override
    public int getItemCount() {
        return perfumes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView perfumeImage;
        TextView perfumeName, perfumeNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            perfumeImage = itemView.findViewById(R.id.perfume_image);
            perfumeName = itemView.findViewById(R.id.perfume_name);
            perfumeNotes = itemView.findViewById(R.id.perfume_notes);
        }
    }
}
