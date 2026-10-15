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
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.EntryForm;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteHorsesAdapter extends RecyclerView.Adapter<FavoriteHorsesAdapter.ViewHolder> {

    private final List<String> horseNames;
    private final OnRemoveClickListener listener;
    private final OnResultClickListener resultClickListener;
    private final OnEntryClickListener entryClickListener;
    // 馬名 → 直近の結果。null の間は読み込み中
    private Map<String, HorseResult> latestResults;
    // 馬名（印を外した名前）→ いちばん近い出走予定。出走予定がない馬は入っていない
    private Map<String, SupabaseManager.RaceEntry> nextEntries = new HashMap<>();

    public interface OnRemoveClickListener {
        void onRemoveClick(String horseName);
    }

    public interface OnResultClickListener {
        void onResultClick(HorseResult result);
    }

    public interface OnEntryClickListener {
        void onEntryClick(SupabaseManager.RaceEntry entry);
    }

    public FavoriteHorsesAdapter(List<String> horseNames, OnRemoveClickListener listener,
                                 OnResultClickListener resultClickListener,
                                 OnEntryClickListener entryClickListener) {
        this.horseNames = horseNames;
        this.listener = listener;
        this.resultClickListener = resultClickListener;
        this.entryClickListener = entryClickListener;
    }

    public void setNextEntries(Map<String, SupabaseManager.RaceEntry> nextEntries) {
        this.nextEntries = nextEntries != null ? nextEntries : new HashMap<>();
        notifyDataSetChanged();
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

        SupabaseManager.RaceEntry next = nextEntries.get(FavoriteHorseNames.normalize(name));
        if (next == null) {
            holder.nextEntryView.setVisibility(View.GONE);
            holder.nextEntryView.setOnClickListener(null);
        } else {
            holder.nextEntryView.setText(nextEntryLabel(next));
            holder.nextEntryView.setVisibility(View.VISIBLE);
            holder.nextEntryView.setOnClickListener(v -> entryClickListener.onEntryClick(next));
        }

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

    // 例: 「★出走予定: 10/18土 東京11R 富士S（芝1600m）5番 ›」
    private static String nextEntryLabel(SupabaseManager.RaceEntry e) {
        StringBuilder sb = new StringBuilder("★出走予定: ");
        sb.append(WeekendDays.formatShort(e.getKaisaibi())).append(' ')
                .append(e.getKaisaijo()).append(e.getRaceNo()).append('R');
        String title = e.getRaceTitle();
        if (title != null && !title.trim().isEmpty()) sb.append(' ').append(title.trim());
        String course = EntryForm.courseLabel(e.getRaceInfo());
        if (!course.isEmpty()) sb.append('（').append(course).append("m）");
        String number = e.getHorseNumber();
        if (number != null && !number.trim().isEmpty()) sb.append(' ').append(number.trim()).append('番');
        return sb.append(" ›").toString();
    }

    @Override
    public int getItemCount() {
        return horseNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView latestResultView;
        TextView nextEntryView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_horse_name);
            latestResultView = itemView.findViewById(R.id.text_latest_result);
            nextEntryView = itemView.findViewById(R.id.text_next_entry);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
