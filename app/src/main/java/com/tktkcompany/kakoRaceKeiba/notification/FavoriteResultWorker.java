package com.tktkcompany.kakoRaceKeiba.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tktkcompany.kakoRaceKeiba.db.FavoriteResultFetcher;
import com.tktkcompany.kakoRaceKeiba.db.HorseResult;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * お気に入り馬の新しいレース結果を定期的に確認し、見つかったら通知する。
 */
public class FavoriteResultWorker extends Worker {

    private static final String TAG = "FavoriteResultWorker";
    private static final String WORK_NAME = "favorite_result_check";
    private static final String PREFS = "favorite_result_worker";
    private static final String KEY_NOTIFIED = "notified_keys_";
    // 何日前までの結果を通知対象にするか（古い結果をまとめて通知しないため）
    private static final int RECENT_DAYS = 3;

    public FavoriteResultWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /** 定期チェックを登録する（登録済みならそのまま） */
    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                FavoriteResultWorker.class, 3, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return Result.success();
        Context context = getApplicationContext();
        if (!NotificationHelper.canPostNotifications(context)) return Result.success();

        try {
            Set<String> favorites = new HashSet<>(FavoriteResultFetcher.fetchFavoriteHorseKeys(user.getUid()));
            if (favorites.isEmpty()) return Result.success();

            List<String> joNames = FavoriteResultFetcher.fetchActiveJoNames();
            Map<String, List<HorseResult>> results = FavoriteResultFetcher.fetchResults(favorites, joNames);

            String since = LocalDate.now().minusDays(RECENT_DAYS).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String prefKey = KEY_NOTIFIED + user.getUid();
            Set<String> notified = prefs.getStringSet(prefKey, new HashSet<>());
            Set<String> stillRecent = new HashSet<>();

            for (List<HorseResult> list : results.values()) {
                for (HorseResult r : list) {
                    if (r.kaisaibi == null || r.kaisaibi.compareTo(since) < 0) continue;
                    stillRecent.add(r.key);
                    if (notified.contains(r.key)) continue;
                    NotificationHelper.show(context, NotificationHelper.FAVORITE_CHANNEL_ID,
                            r.key.hashCode(),
                            "★ " + r.horseName + " の結果",
                            r.summary());
                }
            }

            // 通知済みの記録は直近分だけ残す（増え続けないように）
            prefs.edit().putStringSet(prefKey, stillRecent).apply();
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "お気に入り馬の結果確認に失敗", e);
            return Result.retry();
        }
    }
}
