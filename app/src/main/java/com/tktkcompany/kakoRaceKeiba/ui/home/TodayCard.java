package com.tktkcompany.kakoRaceKeiba.ui.home;

import com.tktkcompany.kakoRaceKeiba.util.Lang;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.LiveResults;
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
 * 残り時間は1分ごとに書き直し、結果は確定したレースが変わったときだけ読み直す。
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
            addRow("★", 0xFFE65100, e.getHorseName() + "　" + Lang.race(e.getKaisaijo(), e.getRaceNo()) + " " + start.format(HM),
                    remaining(now, start), null, v -> openEntry(e));
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

            CharSequence result = null;
            if (lastFinished != null) {
                String key = lastFinished.getRaceNo() + "R";
                if (!key.equals(latestResultKey.get(jo))) {
                    latestResultKey.put(jo, key);
                    latestResultText.remove(jo);
                    loadLatestResult(jo, lastFinished.getRaceNo());
                }
                result = latestResultText.get(jo);
            }

            if (next != null) {
                LocalDateTime start = start(next);
                SupabaseManager.RaceEntry target = next;
                addRow(Lang.course(jo), 0xFF2E7D32, Lang.t(next.getRaceNo() + "R " + start.format(HM) + "発走", "R" + next.getRaceNo() + " " + start.format(HM)), remaining(now, start), result,
                        v -> openEntry(target));
            } else {
                addRow(Lang.course(jo), 0xFF2E7D32, Lang.t("全レース終了", "All races finished"), null, result, v -> openResults(jo));
            }
        }
        // 残り時間を1分ごとに書き直す
        handler.postDelayed(tick, 60_000);
    }

    /** 確定したばかりのレースの1〜3着を読む */
    private void loadLatestResult(String jo, int raceNo) {
        EXECUTOR.execute(() -> {
            List<SupabaseManager.RaceResult> rows;
            try {
                rows = LiveResults.fetchRaceBlocking(today, jo, raceNo);
            } catch (Exception e) {
                Log.w(TAG, "直前の結果の取得に失敗: " + jo + raceNo + "R", e);
                return;
            }
            if (rows.isEmpty()) {
                // まだ確定していない。次に書き直すときにもう一度取りに行く
                handler.post(() -> latestResultKey.remove(jo));
                return;
            }
            // 「6R 1着 チュウワアーチ（1人気）」。勝ち馬だけにして1行に収める
            SpannableStringBuilder text = new SpannableStringBuilder();
            for (SupabaseManager.RaceResult r : rows) {
                if (!"1".equals(r.getRank())) continue;
                text.append(Lang.t(raceNo + "R ", "R" + raceNo + " "));
                appendColored(text, Lang.rank("1"), 0xFFC62828, true);
                text.append(" ").append(r.getHorseName() != null ? r.getHorseName() : "");
                if (r.getPopular() != null) text.append(Lang.t("（" + r.getPopular() + "人気）", " (" + Lang.popularity(r.getPopular()) + ")"));
                break;
            }
            handler.post(() -> {
                latestResultText.put(jo, text);
                render();
            });
        });
    }

    /**
     * 1行: 左に見出し（競馬場名か★）、真ん中に「7R 13:15発走」、右に残り時間。
     * 下に小さく直前のレースの勝ち馬（あれば）
     */
    private void addRow(String label, int labelColor, String main, @Nullable String countdown,
                        @Nullable CharSequence sub, View.OnClickListener onClick) {
        float density = fragment.getResources().getDisplayMetrics().density;
        LinearLayout block = new LinearLayout(fragment.requireContext());
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(0, (int) (6 * density), 0, (int) (6 * density));
        block.setOnClickListener(onClick);

        LinearLayout row = new LinearLayout(fragment.requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView head = new TextView(fragment.requireContext());
        head.setText(label);
        head.setTextSize(15);
        head.setTextColor(labelColor);
        head.setTypeface(head.getTypeface(), Typeface.BOLD);
        head.setMinWidth((int) (48 * density));
        row.addView(head);

        TextView body = new TextView(fragment.requireContext());
        body.setText(main);
        body.setTextSize(15);
        body.setTextColor(0xFF222222);
        body.setMaxLines(1);
        body.setEllipsize(TextUtils.TruncateAt.END);
        row.addView(body, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        if (countdown != null) {
            TextView badge = new TextView(fragment.requireContext());
            badge.setText(countdown);
            badge.setTextSize(13);
            badge.setTextColor(0xFFC62828);
            badge.setTypeface(badge.getTypeface(), Typeface.BOLD);
            badge.setPadding((int) (8 * density), (int) (2 * density), (int) (8 * density), (int) (2 * density));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(10 * density);
            bg.setColor(0xFFFFEBEE);
            badge.setBackground(bg);
            row.addView(badge);
        }
        block.addView(row);

        if (sub != null && sub.length() > 0) {
            TextView small = new TextView(fragment.requireContext());
            small.setText(sub);
            small.setTextSize(12);
            small.setTextColor(0xFF666666);
            small.setMaxLines(1);
            small.setEllipsize(TextUtils.TruncateAt.END);
            small.setPadding((int) (48 * density), (int) (2 * density), 0, 0);
            block.addView(small);
        }
        container.addView(block);
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

    /** 「あと12分」「あと1時間5分」 */
    static String remaining(LocalDateTime now, LocalDateTime start) {
        long minutes = Math.max(0, Duration.between(now, start).toMinutes());
        if (minutes < 1) return Lang.t("まもなく", "Soon");
        if (minutes < 60) return Lang.t("あと" + minutes + "分", "in " + minutes + " min");
        return Lang.t("あと" + (minutes / 60) + "時間" + (minutes % 60) + "分", "in " + (minutes / 60) + "h " + (minutes % 60) + "m");
    }

    private static void appendColored(SpannableStringBuilder sb, String text, int color, boolean bold) {
        int from = sb.length();
        sb.append(text);
        sb.setSpan(new ForegroundColorSpan(color), from, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bold) sb.setSpan(new StyleSpan(Typeface.BOLD), from, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
