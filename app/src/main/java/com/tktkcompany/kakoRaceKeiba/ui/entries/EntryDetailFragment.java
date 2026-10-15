package com.tktkcompany.kakoRaceKeiba.ui.entries;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentEntriesBinding;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemoDialog;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemos;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 1レース分の出走馬と、それぞれの近走成績・予想の手がかりを表示する */
public class EntryDetailFragment extends Fragment {

    private static final int RECENT_RUNS = 5;

    // 枠番ごとの色（1白 2黒 3赤 4青 5黄 6緑 7橙 8桃）
    private static final int[] WAKU_BACKGROUND = {
            0xFFFFFFFF, 0xFF222222, 0xFFE53935, 0xFF1E88E5,
            0xFFFDD835, 0xFF43A047, 0xFFFB8C00, 0xFFF06292};
    private static final int[] WAKU_TEXT = {
            0xFF222222, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
            0xFF222222, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};

    private FragmentEntriesBinding binding;
    private String raceDate;
    // メモを書いた場面として残す「10/17(土) 京都9R もみじステークス」
    private String raceLabel;
    private Set<String> favorites = new HashSet<>();
    private Map<String, HorseMemos.Memo> memos = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntriesBinding.inflate(inflater, container, false);

        Bundle args = getArguments();
        String date = args != null ? args.getString(EntriesFragment.ARG_DATE) : null;
        String course = args != null ? args.getString(EntriesFragment.ARG_COURSE) : null;
        int raceNo = args != null ? args.getInt(EntriesFragment.ARG_RACE_NO) : 0;
        if (date == null || course == null) return binding.getRoot();

        raceDate = date;
        setScreenTitle(WeekendDays.formatShort(date) + " " + course + raceNo + "R");
        String raceTitle = args.getString(EntriesFragment.ARG_RACE_TITLE, "");
        raceLabel = (WeekendDays.formatShort(date) + " " + course + raceNo + "R " + raceTitle.trim()).trim();
        binding.textEntriesLead.setText(raceTitle);
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
                showHeader(entries);
                FavoriteHorseNames.load(loaded -> {
                    if (binding == null || !isAdded()) return;
                    favorites = new HashSet<>(loaded);
                    HorseMemos.load(loadedMemos -> {
                        if (binding == null || !isAdded()) return;
                        memos = new HashMap<>(loadedMemos);
                        loadPastResults(entries);
                    });
                });
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

    /** レース名・条件・枠順確定の状況を画面上部に出す */
    private void showHeader(List<SupabaseManager.RaceEntry> entries) {
        SupabaseManager.RaceEntry first = entries.get(0);
        SpannableStringBuilder text = new SpannableStringBuilder();
        String title = first.getRaceTitle() != null ? first.getRaceTitle() : "";
        text.append(title, new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        String conditions = conditions(first.getRaceInfo());
        if (!conditions.isEmpty()) text.append("\n").append(conditions);

        text.append("\n");
        if (isBeforeDraw(entries)) {
            text.append("枠順確定前（特別登録 " + entries.size() + "頭）。出走できる頭数を超える場合は抽選・除外があります");
        } else {
            text.append(entries.size() + "頭立て");
        }
        text.append("\n各馬の近走" + RECENT_RUNS + "走（新しい順）。行をタップするとそのレースの結果を表示します");
        binding.textEntriesLead.setText(text);
        binding.textEntriesLead.setTextColor(0xFF333333);
    }

    /** 「1勝クラス　(混) 定量 芝2200m 34頭 発走」から頭数と「発走」を除いた条件を返す */
    static String conditions(String raceInfo) {
        if (raceInfo == null) return "";
        return raceInfo.replaceAll("\\s*\\d+頭", "")
                .replaceAll("\\s*発走.*$", "")
                .replaceAll("[\\s　]+", " ")
                .trim();
    }

    private static boolean isBeforeDraw(List<SupabaseManager.RaceEntry> entries) {
        for (SupabaseManager.RaceEntry e : entries) {
            if (e.getHorseNumber() != null) return false;
        }
        return true;
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
            if (entry.getHorseNumber() != null) {
                number.setText(entry.getHorseNumber());
                styleWaku(number, entry.getWaku());
            } else {
                // 枠順確定前は馬番の枠を出さない
                number.setVisibility(View.GONE);
            }

            TextView name = card.findViewById(R.id.text_entry_horse);
            name.setText(entry.getHorseName());
            bindFavorite(card, entry.getHorseName());
            bindMemo(card, entry.getHorseName());

            List<String> right = new ArrayList<>();
            if (entry.getSexAge() != null && !entry.getSexAge().endsWith("0")) right.add(entry.getSexAge());
            if (entry.getJockey() != null) right.add(entry.getJockey());
            if (entry.getWeight() != null) right.add(entry.getWeight());
            ((TextView) card.findViewById(R.id.text_entry_jockey)).setText(android.text.TextUtils.join(" ", right));

            List<SupabaseManager.RaceResult> runs = results.get(entry.getHorseName());
            showBadges(card.findViewById(R.id.entry_badges), entry, runs);

            LinearLayout pastContainer = card.findViewById(R.id.entry_past_results);
            if (runs == null || runs.isEmpty()) {
                TextView none = new TextView(requireContext());
                none.setText("過去の成績データはありません");
                none.setTextSize(12);
                none.setTextColor(0xFF999999);
                none.setPadding(0, 6, 0, 6);
                pastContainer.addView(none);
            } else {
                for (SupabaseManager.RaceResult run : runs) {
                    pastContainer.addView(createPastRunView(inflater, pastContainer, run));
                }
            }
            binding.entriesContainer.addView(card);
        }
        // 行がフォーカスを取ってスクロール位置がずれないよう、先頭から表示する
        binding.getRoot().post(() -> {
            if (binding != null) binding.getRoot().scrollTo(0, 0);
        });
    }

    private void bindFavorite(View card, String horseName) {
        TextView star = card.findViewById(R.id.text_entry_favorite);
        View cardView = card.findViewById(R.id.card_entry_horse);
        Runnable paint = () -> {
            boolean fav = FavoriteHorseNames.contains(favorites, horseName);
            star.setText(fav ? "★" : "☆");
            star.setTextColor(fav ? 0xFFFFB300 : 0xFFBDBDBD);
            ((MaterialCardView) cardView)
                    .setCardBackgroundColor(fav ? 0xFFFFF8E1 : 0xFFFFFFFF);
        };
        paint.run();
        star.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getContext(), "お気に入り機能を利用するにはログインが必要です", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean added = FavoriteHorseNames.toggle(favorites, horseName);
            paint.run();
            Toast.makeText(getContext(), horseName + (added ? "をお気に入りに追加しました" : "をお気に入りから削除しました"), Toast.LENGTH_SHORT).show();
        });
    }

    private void bindMemo(View card, String horseName) {
        TextView button = card.findViewById(R.id.text_entry_memo);
        TextView body = card.findViewById(R.id.text_entry_memo_body);
        Runnable paint = () -> {
            HorseMemos.Memo memo = HorseMemos.find(memos, horseName);
            button.setAlpha(memo != null ? 1f : 0.35f);
            if (memo == null) {
                body.setVisibility(View.GONE);
            } else {
                body.setText("📝 " + memo.text);
                body.setVisibility(View.VISIBLE);
            }
        };
        paint.run();
        View.OnClickListener edit = v -> HorseMemoDialog.show(requireContext(), memos, horseName, raceLabel,
                memo -> paint.run());
        button.setOnClickListener(edit);
        body.setOnClickListener(edit);
    }

    private void showBadges(ChipGroup group, SupabaseManager.RaceEntry entry, List<SupabaseManager.RaceResult> runs) {
        List<EntryForm.Run> formRuns = new ArrayList<>();
        if (runs != null) {
            for (SupabaseManager.RaceResult r : runs) {
                formRuns.add(new EntryForm.Run(r.getKaisaibi(), r.getHassouTime(), r.getRank(), r.getJockey()));
            }
        }
        List<String> badges = EntryForm.badges(raceDate, entry.getRaceInfo(), entry.getJockey(), formRuns);
        if (badges.isEmpty()) {
            group.setVisibility(View.GONE);
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        for (String text : badges) {
            // チップより背の低い枠線つきのラベルにする（1頭あたりの高さを抑えるため）
            boolean caution = text.equals("休み明け") || text.equals("連闘") || text.equals("乗り替わり") || text.contains("→");
            TextView badge = new TextView(requireContext());
            badge.setText(text);
            badge.setTextSize(11);
            badge.setTextColor(caution ? 0xFFE65100 : 0xFF2E7D32);
            badge.setPadding((int) (8 * density), (int) (2 * density), (int) (8 * density), (int) (2 * density));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(10 * density);
            bg.setStroke((int) Math.max(1, density), caution ? 0xFFFB8C00 : 0xFF43A047);
            badge.setBackground(bg);
            group.addView(badge);
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

    private View createPastRunView(LayoutInflater inflater, ViewGroup parent, SupabaseManager.RaceResult run) {
        View row = inflater.inflate(R.layout.item_past_run, parent, false);
        String date = WeekendDays.formatShort(run.getKaisaibi());
        // 「10/12(月)」→「10/12」（列の幅を揃えるため曜日は省く）
        int paren = date.indexOf('(');
        ((TextView) row.findViewById(R.id.text_run_date)).setText(paren > 0 ? date.substring(0, paren) : date);
        ((TextView) row.findViewById(R.id.text_run_course)).setText(run.getKaisaijo());
        ((TextView) row.findViewById(R.id.text_run_title)).setText(run.getRaceTitle() != null ? run.getRaceTitle().trim() : "");
        ((TextView) row.findViewById(R.id.text_run_distance)).setText(EntryForm.courseLabel(run.getHassouTime()));

        String rank = run.getRank() != null ? run.getRank() : "-";
        String result = rank.matches("\\d+") ? rank + "着" : rank;
        if (run.getPopular() != null && !run.getPopular().isEmpty()) result += " " + run.getPopular() + "人";
        TextView resultView = row.findViewById(R.id.text_run_result);
        resultView.setText(result);
        if ("1".equals(rank)) {
            resultView.setTextColor(0xFFC62828);
            resultView.setTypeface(resultView.getTypeface(), Typeface.BOLD);
        } else if ("2".equals(rank) || "3".equals(rank)) {
            resultView.setTextColor(0xFF2E7D32);
            resultView.setTypeface(resultView.getTypeface(), Typeface.BOLD);
        } else {
            resultView.setTextColor(0xFF333333);
        }

        row.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("key", run.getKaisaibi());
            bundle.putString("jo", run.getKaisaijo());
            Navigation.findNavController(v).navigate(R.id.navigation_raceResults, bundle);
        });
        return row;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
