package com.tktkcompany.kakoRaceKeiba.ui.home;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabResults;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.notification.RaceStartNotices;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ホームの「今日の開催」カード。開催日だけ表示する。
 * 競馬場ごとの次のレース（残り時間つき）、お気に入り馬が出る次のレース、確定したばかりのレースの結果を出す。
 * 残り時間は1分ごとに書き直し、結果は確定したレースが変わったときだけ keibalab から取り直す。
 */
class TodayCard {

    private static final String TAG = "TodayCard";
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("H:mm");
    // 発走からこの分数がたったレースは結果が出ているはず
    private static final int RESULT_AFTER_MINUTES = 10;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Fragment fragment;
    private final MaterialCardView card;
    private final LinearLayout container;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = this::render;

    private String today;
    // 競馬場 → レース番号 → そのレースの1頭目（レース名・発走時刻を見るため）
    private final Map<String, TreeMap<Integer, SupabaseManager.RaceEntry>> races = new LinkedHashMap<>();
    // 今日のお気に入り馬の出走（発走順）
    private final List<SupabaseManager.RaceEntry> favoriteRuns = new ArrayList<>();
    // 競馬場 → 取得済みの直前の結果（「7R」と1着〜3着の文）
    private final Map<String, String> latestResultKey = new HashMap<>();
    private final Map<String, CharSequence> latestResultText = new HashMap<>();

    TodayCard(Fragment fragment, MaterialCardView card, LinearLayout container) {
        this.fragment = fragment;
        this.card = card;
        this.container = container;
    }

    /** 出馬表とお気に入りを受け取って表示を始める。今日の開催が無ければカードを隠す */
    void show(List<SupabaseManager.RaceEntry> entries, Set<String> favorites) {
        today = LocalDate.now().format(YMD);
        races.clear();
        favoriteRuns.clear();
        for (SupabaseManager.RaceEntry e : entries) {
            if (!today.equals(e.getKaisaibi())) continue;
            TreeMap<Integer, SupabaseManager.RaceEntry> byNo = races.get(e.getKaisaijo());
            if (byNo == null) {
                byNo = new TreeMap<>();
                races.put(e.getKaisaijo(), byNo);
            }
            if (!byNo.containsKey(e.getRaceNo())) byNo.put(e.getRaceNo(), e);
            if (FavoriteHorseNames.contains(favorites, e.getHorseName())) favoriteRuns.add(e);
        }
        favoriteRuns.sort((a, b) -> {
            LocalDateTime sa = start(a), sb = start(b);
            if (sa == null || sb == null) return 0;
            return sa.compareTo(sb);
        });
        if (races.isEmpty()) {
            card.setVisibility(View.GONE);
            return;
        }
        card.setVisibility(View.VISIBLE);
        render();
    }

    void stop() {
        handler.removeCallbacks(tick);
    }

    private void render() {
        handler.removeCallbacks(tick);
        if (!fragment.isAdded() || fragment.getView() == null) return;
        container.removeAllViews();
        LocalDateTime now = LocalDateTime.now();

        // お気に入り馬が出る次のレース
        for (SupabaseManager.RaceEntry e : favoriteRuns) {
            LocalDateTime start = start(e);
            if (start == null || !start.isAfter(now)) continue;
            SpannableStringBuilder text = new SpannableStringBuilder();
            appendColored(text, "★ " + e.getHorseName(), 0xFFE65100, true);
            text.append("  ").append(e.getKaisaijo()).append(String.valueOf(e.getRaceNo())).append("R ")
                    .append(start.format(HM)).append("発走（").append(remaining(now, start)).append("）");
            addLine(text, v -> openEntry(e));
            break;
        }

        for (Map.Entry<String, TreeMap<Integer, SupabaseManager.RaceEntry>> course : races.entrySet()) {
            String jo = course.getKey();
            SupabaseManager.RaceEntry next = null;
            SupabaseManager.RaceEntry lastFinished = null;
            for (SupabaseManager.RaceEntry e : course.getValue().values()) {
                LocalDateTime start = start(e);
                if (start == null) continue;
                if (start.isAfter(now)) {
                    if (next == null) next = e;
                } else if (start.plusMinutes(RESULT_AFTER_MINUTES).isBefore(now)) {
                    lastFinished = e;
                }
            }

            SpannableStringBuilder head = new SpannableStringBuilder();
            appendColored(head, jo, 0xFF2E7D32, true);
            if (next != null) {
                LocalDateTime start = start(next);
                head.append("  次は ").append(String.valueOf(next.getRaceNo())).append("R ").append(shortTitle(next))
                        .append("\n").append(start.format(HM)).append("発走（");
                appendColored(head, remaining(now, start), 0xFFC62828, true);
                head.append("）");
                SupabaseManager.RaceEntry target = next;
                addLine(head, v -> openEntry(target));
            } else {
                head.append("  今日のレースは終わりました");
                addLine(head, v -> openResults(jo));
            }

            if (lastFinished != null) {
                String key = lastFinished.getRaceNo() + "R";
                CharSequence result = latestResultText.get(jo);
                if (!key.equals(latestResultKey.get(jo))) {
                    latestResultKey.put(jo, key);
                    latestResultText.remove(jo);
                    loadLatestResult(jo, lastFinished.getRaceNo());
                }
                if (result != null) addLine(result, v -> openResults(jo));
            }
        }
        // 残り時間を1分ごとに書き直す
        handler.postDelayed(tick, 60_000);
    }

    /** 確定したばかりのレースの1〜3着を keibalab から取る */
    private void loadLatestResult(String jo, int raceNo) {
        EXECUTOR.execute(() -> {
            List<SupabaseManager.RaceResult> rows;
            try {
                rows = KeibalabResults.fetchRaceBlocking(today, jo, raceNo);
            } catch (Exception e) {
                Log.w(TAG, "直前の結果の取得に失敗: " + jo + raceNo + "R", e);
                return;
            }
            if (rows.isEmpty()) {
                // まだ確定していない。次に書き直すときにもう一度取りに行く
                handler.post(() -> latestResultKey.remove(jo));
                return;
            }
            SpannableStringBuilder text = new SpannableStringBuilder();
            text.append("　結果 ").append(String.valueOf(raceNo)).append("R: ");
            int shown = 0;
            for (SupabaseManager.RaceResult r : rows) {
                if (shown >= 3) break;
                if (r.getRank() == null || !r.getRank().matches("\\d+")) continue;
                if (shown > 0) text.append(" ");
                appendColored(text, r.getRank() + "着", "1".equals(r.getRank()) ? 0xFFC62828 : 0xFF2E7D32, true);
                text.append(r.getHorseName() != null ? r.getHorseName() : "");
                if (r.getPopular() != null) text.append("(").append(r.getPopular()).append("人)");
                shown++;
            }
            handler.post(() -> {
                latestResultText.put(jo, text);
                render();
            });
        });
    }

    private void addLine(CharSequence text, View.OnClickListener onClick) {
        TextView tv = new TextView(fragment.requireContext());
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(0xFF222222);
        int pad = (int) (6 * fragment.getResources().getDisplayMetrics().density);
        tv.setPadding(0, pad, 0, pad);
        tv.setOnClickListener(onClick);
        container.addView(tv);
    }

    private void openEntry(SupabaseManager.RaceEntry e) {
        if (fragment.getView() == null) return;
        FavoriteHorseNames.openEntry(fragment.getView(), e);
    }

    private void openResults(String jo) {
        if (fragment.getView() == null) return;
        Bundle bundle = new Bundle();
        bundle.putString("key", today);
        bundle.putString("jo", jo);
        Navigation.findNavController(fragment.getView()).navigate(R.id.navigation_raceResults, bundle);
    }

    @Nullable
    private static LocalDateTime start(SupabaseManager.RaceEntry e) {
        return RaceStartNotices.startTime(e.getKaisaibi(), e.getRaceInfo());
    }

    /** 「サラ系2歳未勝利」→「2歳未勝利」。特別戦はそのまま */
    private static String shortTitle(SupabaseManager.RaceEntry e) {
        String t = e.getRaceTitle() != null ? e.getRaceTitle().trim() : "";
        return t.replaceFirst("^サラ系", "");
    }

    /** 「あと12分」「あと1時間5分」 */
    static String remaining(LocalDateTime now, LocalDateTime start) {
        long minutes = Math.max(0, Duration.between(now, start).toMinutes());
        if (minutes < 1) return "まもなく";
        if (minutes < 60) return "あと" + minutes + "分";
        return "あと" + (minutes / 60) + "時間" + (minutes % 60) + "分";
    }

    private static void appendColored(SpannableStringBuilder sb, String text, int color, boolean bold) {
        int from = sb.length();
        sb.append(text);
        sb.setSpan(new ForegroundColorSpan(color), from, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bold) sb.setSpan(new StyleSpan(Typeface.BOLD), from, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
