package com.tktkcompany.kakoRaceKeiba.notification;

import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tktkcompany.kakoRaceKeiba.db.FavoriteResultFetcher;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 開催日の朝（{@link RaceDayDigest#HOUR}:{@link RaceDayDigest#MINUTE}）に、その日に走るお気に入り馬をまとめて知らせる。
 * お気に入り馬が出馬表に載ったときに、その開催日の分を予約する。実行時にお気に入りと出馬表を読み直す
 */
public class RaceDayDigestWorker extends Worker {

    private static final String TAG = "RaceDayDigestWorker";
    private static final String ARG_DATE = "date";
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    public RaceDayDigestWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /** date（yyyyMMdd）の朝の通知を予約する。その時刻を過ぎていれば何もしない */
    public static void schedule(Context context, String date) {
        LocalDateTime at;
        try {
            at = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(RaceDayDigest.HOUR, RaceDayDigest.MINUTE);
        } catch (RuntimeException e) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(JST);
        if (!now.isBefore(at)) return;
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(RaceDayDigestWorker.class)
                .setInitialDelay(Duration.between(now, at).toMillis(), TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString(ARG_DATE, date).build())
                .build();
        // 1開催日に1つ。予約し直しても通知は1回だけ
        WorkManager.getInstance(context).enqueueUniqueWork("race_day_digest|" + date, ExistingWorkPolicy.REPLACE, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Context context = getApplicationContext();
        String date = getInputData().getString(ARG_DATE);
        if (user == null || date == null || !NotificationHelper.canPostNotifications(context)) return Result.success();
        // 開催日を過ぎてから動いたときは出さない
        if (!date.equals(LocalDate.now(JST).format(DateTimeFormatter.ofPattern("yyyyMMdd")))) return Result.success();
        try {
            Set<String> favorites = new HashSet<>();
            for (String key : FavoriteResultFetcher.fetchFavoriteHorseKeys(user.getUid())) {
                favorites.add(FavoriteHorseNames.normalize(key));
            }
            if (favorites.isEmpty()) return Result.success();
            List<RaceDayDigest.Run> runs = new ArrayList<>();
            for (SupabaseManager.RaceEntry e : SupabaseManager.fetchUpcomingEntriesBlocking(date)) {
                if (!date.equals(e.getKaisaibi()) || e.getScratched()) continue;
                String key = FavoriteHorseNames.normalize(e.getHorseName());
                if (!favorites.contains(key)) continue;
                runs.add(new RaceDayDigest.Run(key, e.getKaisaijo(), e.getRaceNo(), RaceStartNotices.startTime(date, e.getRaceInfo())));
            }
            if (runs.isEmpty()) return Result.success();
            NotificationHelper.show(context, NotificationHelper.ENTRY_CHANNEL_ID, ("digest" + date).hashCode(),
                    RaceDayDigest.title(date, runs), RaceDayDigest.body(date, runs));
        } catch (Exception e) {
            Log.w(TAG, "朝のお知らせの準備に失敗", e);
            return Result.retry();
        }
        return Result.success();
    }
}
