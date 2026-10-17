package com.tktkcompany.kakoRaceKeiba.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabOdds;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 重賞の発走 {@link RaceStartNotices#MINUTES_BEFORE} 分前に、上位人気とオッズを知らせる。
 * お気に入り馬がいなくても知らせる。設定（{@link #isEnabled}）で止められる。ログインは要らない。
 */
public class GradedRaceWorker extends Worker {

    private static final String TAG = "GradedRaceWorker";
    private static final String WORK_TAG = "graded_race";
    private static final String SETTINGS = "notification_settings";
    private static final String KEY_ENABLED = "graded_race_enabled";
    private static final String PREFS = "graded_race_worker";
    private static final String KEY_NOTIFIED = "notified";
    private static final String ARG_DATE = "date";
    private static final String ARG_COURSE = "course";
    private static final String ARG_RACE_NO = "raceNo";
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
    private static final int TOP_FAVORITES = 3;

    public GradedRaceWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static boolean isEnabled(Context context) {
        return context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, true);
    }

    /** オフにしたら、予約済みの通知も取り消す */
    public static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply();
        if (!enabled) WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
    }

    /** 出馬表から重賞を探して発走前の通知を予約する（1レースにつき1回） */
    public static void scheduleAll(Context context, List<SupabaseManager.RaceEntry> entries) {
        if (!isEnabled(context)) return;
        Set<String> done = new HashSet<>();
        for (SupabaseManager.RaceEntry e : entries) {
            if (!GradedRaceNotices.isGraded(e.getRaceTitle())) continue;
            if (done.add(e.getKaisaibi() + "|" + e.getKaisaijo() + "|" + e.getRaceNo())) schedule(context, e);
        }
    }

    private static void schedule(Context context, SupabaseManager.RaceEntry race) {
        LocalDateTime start = RaceStartNotices.startTime(race.getKaisaibi(), race.getRaceInfo());
        if (start == null) return;
        LocalDateTime now = LocalDateTime.now(JST);
        if (!now.isBefore(start)) return;
        long delayMillis = Math.max(0,
                Duration.between(now, start.minusMinutes(RaceStartNotices.MINUTES_BEFORE)).toMillis());
        Data input = new Data.Builder()
                .putString(ARG_DATE, race.getKaisaibi())
                .putString(ARG_COURSE, race.getKaisaijo())
                .putInt(ARG_RACE_NO, race.getRaceNo())
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(GradedRaceWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(input)
                .addTag(WORK_TAG)
                .build();
        String name = "graded_start|" + race.getKaisaibi() + "|" + race.getKaisaijo() + "|" + race.getRaceNo();
        WorkManager.getInstance(context).enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        if (!isEnabled(context) || !NotificationHelper.canPostNotifications(context)) return Result.success();
        String date = getInputData().getString(ARG_DATE);
        String course = getInputData().getString(ARG_COURSE);
        int raceNo = getInputData().getInt(ARG_RACE_NO, 0);
        if (date == null || course == null) return Result.success();
        try {
            List<SupabaseManager.RaceEntry> horses = new ArrayList<>();
            for (SupabaseManager.RaceEntry e : SupabaseManager.fetchUpcomingEntriesBlocking(date)) {
                if (date.equals(e.getKaisaibi()) && course.equals(e.getKaisaijo()) && e.getRaceNo() == raceNo) horses.add(e);
            }
            if (horses.isEmpty()) return Result.success();
            SupabaseManager.RaceEntry race = horses.get(0);
            LocalDateTime start = RaceStartNotices.startTime(date, race.getRaceInfo());
            if (start == null || !LocalDateTime.now(JST).isBefore(start)) return Result.success();

            String raceKey = date + "|" + course + "|" + raceNo;
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String today = LocalDate.now(JST).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Set<String> kept = new HashSet<>();
            for (String key : prefs.getStringSet(KEY_NOTIFIED, new HashSet<>())) {
                if (key.compareTo(today) >= 0) kept.add(key);
            }
            if (!kept.add(raceKey)) return Result.success();

            // 上位人気（オッズが取れなければ馬名なしで発走時刻だけ知らせる）
            List<GradedRaceNotices.Favorite> favorites = new ArrayList<>();
            String oddsTime = null;
            try {
                Map<String, String> odds = KeibalabOdds.fetchWinOddsBlocking(date, course, raceNo);
                for (int popularity = 1; popularity <= TOP_FAVORITES; popularity++) {
                    for (SupabaseManager.RaceEntry e : horses) {
                        Integer p = e.getHorseNumber() != null ? KeibalabOdds.popularity(odds, e.getHorseNumber()) : null;
                        if (p != null && p == popularity) {
                            favorites.add(new GradedRaceNotices.Favorite(p, e.getHorseName(), KeibalabOdds.oddsOf(odds, e.getHorseNumber())));
                            break;
                        }
                    }
                }
                if (!favorites.isEmpty()) oddsTime = LocalTime.now(JST).format(DateTimeFormatter.ofPattern("H:mm"));
            } catch (Exception e) {
                Log.w(TAG, "オッズの取得に失敗（オッズなしで通知する）", e);
            }

            NotificationHelper.show(context, NotificationHelper.GRADED_RACE_CHANNEL_ID,
                    ("graded" + raceKey).hashCode(),
                    GradedRaceNotices.title(race.getRaceTitle()),
                    GradedRaceNotices.body(start, course, raceNo, race.getRaceInfo(), favorites, oddsTime),
                    MainActivity.openRaceExtras(date, course, raceNo, race.getRaceTitle()));
            prefs.edit().putStringSet(KEY_NOTIFIED, kept).apply();
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "重賞の通知の確認に失敗", e);
            return Result.retry();
        }
    }
}
