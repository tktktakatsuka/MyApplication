package com.tktkcompany.kakoRaceKeiba.ui.favorite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tktkcompany.kakoRaceKeiba.R;

import java.util.List;

public class FavoriteHorsesAdapter extends RecyclerView.Adapter<FavoriteHorsesAdapter.ViewHolder> {

    private final List<String> horseNames;
    private final OnRemoveClickListener listener;

    public interface OnRemoveClickListener {
        void onRemoveClick(String horseName);
    }

    public FavoriteHorsesAdapter(List<String> horseNames, OnRemoveClickListener listener) {
        this.horseNames = horseNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_horse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = horseNames.get(position);
        holder.textView.setText(name);
        holder.deleteButton.setOnClickListener(v -> listener.onRemoveClick(name));
    }

    @Override
    public int getItemCount() {
        return horseNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_horse_name);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
