package com.tktkcompany.kakoRaceKeiba.ui.entries;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentEntriesBinding;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 今週の出走馬：開催日・競馬場ごとにレースを並べ、タップで出走馬の近走成績へ進む */
public class EntriesFragment extends Fragment {

    public static final String ARG_DATE = "date";
    public static final String ARG_COURSE = "course";
    public static final String ARG_RACE_NO = "raceNo";
    public static final String ARG_RACE_TITLE = "raceTitle";

    private FragmentEntriesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntriesBinding.inflate(inflater, container, false);
        binding.textEntriesLead.setText("レースを選ぶと、出走馬ごとの近走成績を確認できます");
        loadEntries();
        return binding.getRoot();
    }

    private void loadEntries() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        SupabaseManager.fetchUpcomingEntries(today, new SupabaseManager.SupabaseCallback<List<SupabaseManager.RaceEntry>>() {
            @Override
            public void onSuccess(List<SupabaseManager.RaceEntry> entries) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (entries.isEmpty()) {
                    showEmpty("今週の出走馬はまだ発表されていません。\n出馬表が確定すると、ここに表示されます。");
                    return;
                }
                render(entries);
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("EntriesFragment", "Supabase load failed: " + message);
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                showEmpty("出走馬の読み込みに失敗しました");
            }
        });
    }

    private void showEmpty(String message) {
        binding.textEntriesEmpty.setText(message);
        binding.textEntriesEmpty.setVisibility(View.VISIBLE);
    }

    private void render(List<SupabaseManager.RaceEntry> entries) {
        // 開催日・競馬場 → レース番号 → 出走馬 の順にまとめる（取得時点で並び替え済み）
        Map<String, Map<Integer, List<SupabaseManager.RaceEntry>>> meetings = new LinkedHashMap<>();
        for (SupabaseManager.RaceEntry entry : entries) {
            String key = entry.getKaisaibi() + "\t" + entry.getKaisaijo();
            Map<Integer, List<SupabaseManager.RaceEntry>> races = meetings.get(key);
            if (races == null) {
                races = new LinkedHashMap<>();
                meetings.put(key, races);
            }
            List<SupabaseManager.RaceEntry> horses = races.get(entry.getRaceNo());
            if (horses == null) {
                horses = new ArrayList<>();
                races.put(entry.getRaceNo(), horses);
            }
            horses.add(entry);
        }

        LayoutInflater inflater = getLayoutInflater();
        binding.entriesContainer.removeAllViews();
        for (Map.Entry<String, Map<Integer, List<SupabaseManager.RaceEntry>>> meeting : meetings.entrySet()) {
            String[] key = meeting.getKey().split("\t");
            String date = key[0];
            String course = key[1];

            TextView header = new TextView(requireContext());
            header.setText(WeekendDays.formatShort(date) + " " + course);
            header.setTextSize(18);
            header.setTextColor(0xFF333333);
            header.setTypeface(header.getTypeface(), Typeface.BOLD);
            header.setPadding(0, 32, 0, 8);
            binding.entriesContainer.addView(header);

            for (Map.Entry<Integer, List<SupabaseManager.RaceEntry>> race : meeting.getValue().entrySet()) {
                int raceNo = race.getKey();
                SupabaseManager.RaceEntry first = race.getValue().get(0);
                String title = first.getRaceTitle() != null ? first.getRaceTitle() : "";

                View item = inflater.inflate(R.layout.item_race_day, binding.entriesContainer, false);
                ((TextView) item.findViewById(R.id.text_race_day_date)).setText(raceNo + "R");
                ((TextView) item.findViewById(R.id.text_race_day_main)).setText(title + "（" + race.getValue().size() + "頭）");
                item.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString(ARG_DATE, date);
                    args.putString(ARG_COURSE, course);
                    args.putInt(ARG_RACE_NO, raceNo);
                    args.putString(ARG_RACE_TITLE, title);
                    Navigation.findNavController(v).navigate(R.id.action_navigation_entries_to_navigation_entryDetail, args);
                });
                binding.entriesContainer.addView(item);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
