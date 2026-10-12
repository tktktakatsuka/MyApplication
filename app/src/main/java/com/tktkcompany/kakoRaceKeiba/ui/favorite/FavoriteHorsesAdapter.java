package com.tktkcompany.kakoRaceKeiba.ui.favorite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.HorseResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteHorsesAdapter extends RecyclerView.Adapter<FavoriteHorsesAdapter.ViewHolder> {

    private final List<String> horseNames;
    private final OnRemoveClickListener listener;
    private final OnResultClickListener resultClickListener;
    // 馬名 → 直近の結果。null の間は読み込み中
    private Map<String, HorseResult> latestResults;

    public interface OnRemoveClickListener {
        void onRemoveClick(String horseName);
    }

    public interface OnResultClickListener {
        void onResultClick(HorseResult result);
    }

    public FavoriteHorsesAdapter(List<String> horseNames, OnRemoveClickListener listener,
                                 OnResultClickListener resultClickListener) {
        this.horseNames = horseNames;
        this.listener = listener;
        this.resultClickListener = resultClickListener;
    }

    public void setLatestResults(Map<String, HorseResult> latestResults) {
        this.latestResults = latestResults != null ? latestResults : new HashMap<>();
        notifyDataSetChanged();
    }

    public void clearLatestResults() {
        this.latestResults = null;
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

        HorseResult latest = latestResults != null ? latestResults.get(name) : null;
        if (latestResults == null) {
            holder.latestResultView.setText("直近の結果を読み込み中…");
            holder.latestResultView.setOnClickListener(null);
        } else if (latest == null) {
            holder.latestResultView.setText("開催中の競馬場での出走はありません");
            holder.latestResultView.setOnClickListener(null);
        } else {
            holder.latestResultView.setText("直近: " + latest.summary() + " ›");
            holder.latestResultView.setOnClickListener(v -> resultClickListener.onResultClick(latest));
        }
    }

    @Override
    public int getItemCount() {
        return horseNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView latestResultView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_horse_name);
            latestResultView = itemView.findViewById(R.id.text_latest_result);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
