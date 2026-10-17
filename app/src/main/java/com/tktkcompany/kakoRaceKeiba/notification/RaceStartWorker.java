package com.tktkcompany.kakoRaceKeiba.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tktkcompany.kakoRaceKeiba.db.FavoriteResultFetcher;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabOdds;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * お気に入り馬が出るレースの発走 {@link RaceStartNotices#MINUTES_BEFORE} 分前に知らせる。
 * 1レースにつき1つの作業を予約し、実行時にお気に入りと出馬表を読み直してから通知する
 * （予約後にお気に入りから外した馬や、取消になった馬を知らせないため）。
 */
public class RaceStartWorker extends Worker {

    private static final String TAG = "RaceStartWorker";
    private static final String PREFS = "race_start_worker";
    private static final String KEY_NOTIFIED = "notified_";
    private static final String ARG_DATE = "date";
    private static final String ARG_COURSE = "course";
    private static final String ARG_RACE_NO = "raceNo";
    // 発走時刻は JRA の時刻（日本時間）
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    public RaceStartWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /** レースの発走前の通知を予約する。発走時刻が読めないか、もう発走していれば何もしない */
    public static void schedule(Context context, SupabaseManager.RaceEntry race) {
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
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(RaceStartWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(input)
                .build();
        // 定期チェックのたびに予約し直す（発走時刻が変わっても追いつくように）。通知済みかは実行時に確かめる
        String name = "race_start|" + race.getKaisaibi() + "|" + race.getKaisaijo() + "|" + race.getRaceNo();
        WorkManager.getInstance(context).enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return Result.success();
        Context context = getApplicationContext();
        if (!NotificationHelper.canPostNotifications(context)) return Result.success();

        String date = getInputData().getString(ARG_DATE);
        String course = getInputData().getString(ARG_COURSE);
        int raceNo = getInputData().getInt(ARG_RACE_NO, 0);
        if (date == null || course == null) return Result.success();

        try {
            Set<String> favorites = new HashSet<>();
            for (String key : FavoriteResultFetcher.fetchFavoriteHorseKeys(user.getUid())) {
                favorites.add(FavoriteHorseNames.normalize(key));
            }
            if (favorites.isEmpty()) return Result.success();

            SupabaseManager.RaceEntry race = null;
            List<SupabaseManager.RaceEntry> favoriteEntries = new ArrayList<>();
            for (SupabaseManager.RaceEntry e : SupabaseManager.fetchUpcomingEntriesBlocking(date)) {
                if (!date.equals(e.getKaisaibi()) || !course.equals(e.getKaisaijo()) || e.getRaceNo() != raceNo) continue;
                race = e;
                if (favorites.contains(FavoriteHorseNames.normalize(e.getHorseName()))) favoriteEntries.add(e);
            }
            if (race == null || favoriteEntries.isEmpty()) return Result.success();

            // 発走後に遅れて動いたときは知らせない
            LocalDateTime start = RaceStartNotices.startTime(date, race.getRaceInfo());
            if (start == null || !LocalDateTime.now(JST).isBefore(start)) return Result.success();

            String raceKey = date + "|" + course + "|" + raceNo;
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String prefKey = KEY_NOTIFIED + user.getUid();
            String today = LocalDate.now(JST).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // 開催日が過ぎた記録は消す（増え続けないように）。キーは「開催日|…」
            Set<String> kept = new HashSet<>();
            for (String key : prefs.getStringSet(prefKey, new HashSet<>())) {
                if (key.compareTo(today) >= 0) kept.add(key);
            }
            if (!kept.add(raceKey)) return Result.success();

            // 今の単勝オッズを keibalab から取る。取れなければ、取り込み済みのオッズ（当日のバッチで更新）を使う
            Map<String, String> liveOdds = new HashMap<>();
            String oddsTime = null;
            try {
                liveOdds = KeibalabOdds.fetchWinOddsBlocking(date, course, raceNo);
                if (!liveOdds.isEmpty()) oddsTime = LocalTime.now(JST).format(DateTimeFormatter.ofPattern("H:mm"));
            } catch (Exception e) {
                Log.w(TAG, "オッズの取得に失敗（オッズなしで通知する）", e);
            }
            List<RaceStartNotices.Horse> horses = new ArrayList<>();
            for (SupabaseManager.RaceEntry e : favoriteEntries) {
                String odds = KeibalabOdds.oddsOf(liveOdds, e.getHorseNumber());
                Integer popularity = odds != null ? KeibalabOdds.popularity(liveOdds, e.getHorseNumber()) : null;
                if (odds == null && e.getWinOdds() != null) {
                    odds = e.getWinOdds();
                    popularity = parseIntOrNull(e.getPopular());
                    if (oddsTime == null) oddsTime = storedOddsTime(e.getOddsUpdatedAt());
                }
                horses.add(new RaceStartNotices.Horse(e.getHorseName(), e.getHorseNumber(), odds, popularity));
            }

            NotificationHelper.show(context, NotificationHelper.RACE_START_CHANNEL_ID,
                    ("start" + raceKey).hashCode(),
                    RaceStartNotices.title(horses),
                    RaceStartNotices.body(start, course, raceNo, race.getRaceTitle(), race.getRaceInfo(), horses, oddsTime));
            prefs.edit().putStringSet(prefKey, kept).apply();
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "出走前の通知の確認に失敗", e);
            return Result.retry();
        }
    }

    @Nullable
    private static Integer parseIntOrNull(@Nullable String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 取り込んだ時刻（"2026-10-17T04:15:00+00:00"）を日本時間の "13:15" にする。読めなければ null */
    @Nullable
    private static String storedOddsTime(@Nullable String timestamp) {
        if (timestamp == null) return null;
        try {
            return OffsetDateTime.parse(timestamp).atZoneSameInstant(JST).format(DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeException e) {
            return null;
        }
    }
}
