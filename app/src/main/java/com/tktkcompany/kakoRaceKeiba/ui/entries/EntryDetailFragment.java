package com.tktkcompany.kakoRaceKeiba.ui.entries;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentEntriesBinding;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 1レース分の出走馬と、それぞれの近走成績を表示する */
public class EntryDetailFragment extends Fragment {

    private static final int RECENT_RUNS = 5;
    private static final Pattern DISTANCE = Pattern.compile("(芝|ダ|障)\\D{0,2}(\\d{3,4})m");

    // 枠番ごとの色（1白 2黒 3赤 4青 5黄 6緑 7橙 8桃）
    private static final int[] WAKU_BACKGROUND = {
            0xFFFFFFFF, 0xFF222222, 0xFFE53935, 0xFF1E88E5,
            0xFFFDD835, 0xFF43A047, 0xFFFB8C00, 0xFFF06292};
    private static final int[] WAKU_TEXT = {
            0xFF222222, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
            0xFF222222, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};

    private FragmentEntriesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntriesBinding.inflate(inflater, container, false);

        Bundle args = getArguments();
        String date = args != null ? args.getString(EntriesFragment.ARG_DATE) : null;
        String course = args != null ? args.getString(EntriesFragment.ARG_COURSE) : null;
        int raceNo = args != null ? args.getInt(EntriesFragment.ARG_RACE_NO) : 0;
        String title = args != null ? args.getString(EntriesFragment.ARG_RACE_TITLE, "") : "";
        if (date == null || course == null) return binding.getRoot();

        setScreenTitle(WeekendDays.formatShort(date) + " " + course + raceNo + "R");
        binding.textEntriesLead.setText(title + "\n各馬の近走" + RECENT_RUNS + "走（新しい順）。タップでそのレースの結果へ");
        binding.textEntriesLead.setTextSize(14);
        loadEntries(date, course, raceNo);
        return binding.getRoot();
    }

    private void setScreenTitle(String title) {
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) actionBar.setTitle(title);
        }
    }

    private void loadEntries(String date, String course, int raceNo) {
        SupabaseManager.fetchRaceEntries(date, course, raceNo, new SupabaseManager.SupabaseCallback<List<SupabaseManager.RaceEntry>>() {
            @Override
            public void onSuccess(List<SupabaseManager.RaceEntry> entries) {
                if (binding == null || !isAdded()) return;
                if (entries.isEmpty()) {
                    binding.progressBar.setVisibility(View.GONE);
                    showMessage("出走馬のデータがありません");
                    return;
                }
                loadPastResults(entries);
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("EntryDetailFragment", "Supabase load failed: " + message);
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                showMessage("出走馬の読み込みに失敗しました");
            }
        });
    }

    private void loadPastResults(List<SupabaseManager.RaceEntry> entries) {
        List<String> names = new ArrayList<>();
        for (SupabaseManager.RaceEntry entry : entries) names.add(entry.getHorseName());

        SupabaseManager.fetchRecentResultsForHorses(names, RECENT_RUNS, new SupabaseManager.SupabaseCallback<Map<String, List<SupabaseManager.RaceResult>>>() {
            @Override
            public void onSuccess(Map<String, List<SupabaseManager.RaceResult>> results) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                render(entries, results);
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("EntryDetailFragment", "Past results load failed: " + message);
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                // 成績が取れなくても出走馬の一覧は出す
                render(entries, Collections.emptyMap());
            }
        });
    }

    private void showMessage(String message) {
        binding.textEntriesEmpty.setText(message);
        binding.textEntriesEmpty.setVisibility(View.VISIBLE);
    }

    private void render(List<SupabaseManager.RaceEntry> entries, Map<String, List<SupabaseManager.RaceResult>> results) {
        LayoutInflater inflater = getLayoutInflater();
        binding.entriesContainer.removeAllViews();
        for (SupabaseManager.RaceEntry entry : entries) {
            View card = inflater.inflate(R.layout.item_entry_horse, binding.entriesContainer, false);

            TextView number = card.findViewById(R.id.text_entry_number);
            number.setText(entry.getHorseNumber() != null ? entry.getHorseNumber() : "");
            styleWaku(number, entry.getWaku());

            ((TextView) card.findViewById(R.id.text_entry_horse)).setText(entry.getHorseName());
            String jockey = entry.getJockey() != null ? entry.getJockey() : "";
            if (entry.getWeight() != null) jockey += " " + entry.getWeight();
            ((TextView) card.findViewById(R.id.text_entry_jockey)).setText(jockey);

            LinearLayout pastContainer = card.findViewById(R.id.entry_past_results);
            List<SupabaseManager.RaceResult> runs = results.get(entry.getHorseName());
            if (runs == null || runs.isEmpty()) {
                pastContainer.addView(createPastLine("過去の成績データはありません", null, 0xFF999999));
            } else {
                for (SupabaseManager.RaceResult run : runs) {
                    pastContainer.addView(createPastRunView(run));
                }
            }
            binding.entriesContainer.addView(card);
        }
    }

    private void styleWaku(TextView view, String waku) {
        int index = 0;
        try {
            index = Integer.parseInt(waku != null ? waku.trim() : "") - 1;
        } catch (NumberFormatException ignored) {
        }
        if (index < 0 || index >= WAKU_BACKGROUND.length) index = 0;
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(6 * getResources().getDisplayMetrics().density);
        bg.setColor(WAKU_BACKGROUND[index]);
        bg.setStroke(2, 0xFFBDBDBD);
        view.setBackground(bg);
        view.setTextColor(WAKU_TEXT[index]);
    }

    private View createPastRunView(SupabaseManager.RaceResult run) {
        String rank = run.getRank() != null ? run.getRank() : "-";
        StringBuilder text = new StringBuilder();
        text.append(WeekendDays.formatShort(run.getKaisaibi())).append(" ").append(run.getKaisaijo());
        if (run.getRaceTitle() != null) text.append(" ").append(run.getRaceTitle().trim());
        String distance = extractDistance(run.getHassouTime());
        if (!distance.isEmpty()) text.append(" ").append(distance);
        text.append("　").append(rank.matches("\\d+") ? rank + "着" : rank);
        if (run.getPopular() != null && !run.getPopular().isEmpty()) text.append("（").append(run.getPopular()).append("人気）");

        int color;
        if ("1".equals(rank)) color = 0xFFC62828;
        else if ("2".equals(rank) || "3".equals(rank)) color = 0xFF2E7D32;
        else color = 0xFF333333;

        TextView line = createPastLine(text.toString(), run, color);
        if ("1".equals(rank) || "2".equals(rank) || "3".equals(rank)) {
            line.setTypeface(line.getTypeface(), Typeface.BOLD);
        }
        return line;
    }

    private TextView createPastLine(String text, SupabaseManager.RaceResult run, int color) {
        TextView line = new TextView(requireContext());
        line.setText(text);
        line.setTextSize(13);
        line.setTextColor(color);
        line.setMaxLines(1);
        line.setEllipsize(android.text.TextUtils.TruncateAt.END);
        line.setPadding(0, 6, 0, 6);
        if (run != null) {
            line.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("key", run.getKaisaibi());
                bundle.putString("jo", run.getKaisaijo());
                Navigation.findNavController(v).navigate(R.id.navigation_raceResults, bundle);
            });
        }
        return line;
    }

    private static String extractDistance(String raceInfo) {
        if (raceInfo == null) return "";
        Matcher m = DISTANCE.matcher(raceInfo);
        return m.find() ? m.group(1) + m.group(2) : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
