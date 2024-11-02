package com.example.calculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Glide 라이브러리로 이미지 로드

import java.util.ArrayList;
import java.util.List;
import android.util.Log;



public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {

    private List<Item> items = new ArrayList<>();

    // ViewHolder 클래스 정의
    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView productUrl;
        TextView category;

        public ResultViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            productUrl = itemView.findViewById(R.id.tv_product_url);
            category = itemView.findViewById(R.id.tv_category);
        }
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        Item item = items.get(position);

        holder.productUrl.setText(item.getProductUrl());
        holder.category.setText(item.getCategory());
        // Glide를 사용하여 이미지 로드
        Log.d("ResultAdapter", "Loading image URL: " + item.getThumbnailUrl());
        GlideApp.with(holder.itemView.getContext())
                .load(item.getThumbnailUrl())
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // 데이터 업데이트 메서드
    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}
