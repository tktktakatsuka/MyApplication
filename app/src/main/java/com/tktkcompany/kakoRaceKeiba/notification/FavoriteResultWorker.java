package com.tktkcompany.kakoRaceKeiba.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tktkcompany.kakoRaceKeiba.db.FavoriteResultFetcher;
import com.tktkcompany.kakoRaceKeiba.db.HorseResult;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * お気に入り馬の新しいレース結果と、お気に入り馬・メモした馬の出走予定を定期的に確認し、見つかったら通知する。
 * お気に入り馬が出るレースは、発走前の通知（{@link RaceStartWorker}）もここで予約する。
 */
public class FavoriteResultWorker extends Worker {

    private static final String TAG = "FavoriteResultWorker";
    private static final String WORK_NAME = "favorite_result_check";
    private static final String PREFS = "favorite_result_worker";
    private static final String KEY_NOTIFIED = "notified_keys_";
    private static final String KEY_NOTIFIED_ENTRIES = "notified_entries_";
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
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);    }

    /**
     * 定期チェックを待たずに1回確認する。
     * お気に入りに加えた馬の出走前の通知を、次の定期チェックより前に予約できるようにする。
     */
    public static void runOnce(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(FavoriteResultWorker.class)
                // 直前に加えたお気に入りが Firebase に書き込まれるのを少し待つ
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME + "_now", ExistingWorkPolicy.REPLACE, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        if (!NotificationHelper.canPostNotifications(context)) return Result.success();

        // 重賞の発走前の通知は、ログインしていなくても予約する
        List<SupabaseManager.RaceEntry> upcoming = null;
        try {
            upcoming = SupabaseManager.fetchUpcomingEntriesBlocking(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            GradedRaceWorker.scheduleAll(context, upcoming);
        } catch (Exception e) {
            Log.w(TAG, "出馬表の取得に失敗", e);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return Result.success();

        try {
            Set<String> favorites = new HashSet<>(FavoriteResultFetcher.fetchFavoriteHorseKeys(user.getUid()));
            try {
                if (upcoming != null) notifyUpcomingEntries(context, user.getUid(), favorites, upcoming);
            } catch (Exception e) {
                // 出走予定の確認に失敗しても、結果の確認は続ける（次の定期実行でまた確認する）
                Log.w(TAG, "出走予定の確認に失敗", e);
            }
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
                    // レース直後に速報で知らせた結果は、もう一度は知らせない
                    if (RaceResultWorker.alreadyNotified(context, user.getUid(),
                            LiveResultNotices.key(r.kaisaibi, r.kaisaijo, parseRaceNo(r.raceNo), r.horseName))) continue;
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

    private static int parseRaceNo(String raceNo) {
        try {
            return Integer.parseInt(raceNo.trim());
        } catch (RuntimeException e) {
            return 0;
        }
    }

    /**
     * お気に入り馬・メモした馬が出馬表に載っていたら知らせる。
     * 同じ出走は1回だけ。何頭もいるときは1つの通知にまとめる。
     */
    private void notifyUpcomingEntries(Context context, String uid, Set<String> favoriteKeys,
                                       List<SupabaseManager.RaceEntry> upcoming) throws Exception {
        Set<String> favorites = new HashSet<>();
        for (String key : favoriteKeys) favorites.add(FavoriteHorseNames.normalize(key));
        Map<String, String> memos = FavoriteResultFetcher.fetchHorseMemos(uid);
        if (favorites.isEmpty() && memos.isEmpty()) return;

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String prefKey = KEY_NOTIFIED_ENTRIES + uid;
        Set<String> notified = prefs.getStringSet(prefKey, new HashSet<>());
        // 開催日が過ぎた出走の記録は消す（増え続けないように）。キーは「開催日|…」
        Set<String> kept = new HashSet<>();
        for (String key : notified) {
            if (key.compareTo(today) >= 0) kept.add(key);
        }
        List<EntryNotices.Item> fresh = new ArrayList<>();
        Set<String> scheduledRaces = new HashSet<>();

        for (SupabaseManager.RaceEntry e : upcoming) {
            String key = FavoriteHorseNames.normalize(e.getHorseName());
            boolean favorite = favorites.contains(key);
            String memo = memos.get(key);
            if (!favorite && memo == null) continue;
            // お気に入り馬が出るレースは、発走前の通知も予約する（1レースにつき1回）
            if (favorite && scheduledRaces.add(e.getKaisaibi() + "|" + e.getKaisaijo() + "|" + e.getRaceNo())) {
                RaceStartWorker.schedule(context, e);
                RaceResultWorker.schedule(context, e);
            }
            EntryNotices.Item item = new EntryNotices.Item(e.getHorseName(), e.getKaisaibi(), e.getKaisaijo(),
                    e.getRaceNo(), e.getRaceTitle(), e.getRaceInfo(), favorite, memo);
            if (kept.add(item.key())) fresh.add(item);
        }

        if (!fresh.isEmpty()) {
            NotificationHelper.show(context, NotificationHelper.ENTRY_CHANNEL_ID,
                    ("entries" + fresh.get(0).key()).hashCode(),
                    EntryNotices.title(fresh), EntryNotices.body(fresh));
        }
        prefs.edit().putStringSet(prefKey, kept).apply();
    }
}
