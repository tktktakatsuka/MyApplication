package com.tktkcompany.kakoRaceKeiba.db;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * お気に入り馬の結果を、開催中の競馬場のレース結果(Supabase)から探す。
 * Tasks.await を使うため、必ずバックグラウンドスレッドから呼ぶこと。
 */
public class FavoriteResultFetcher {

    private static final long TIMEOUT_SECONDS = 30;

    /** ログインユーザーのお気に入り馬（Firebaseのキー）を取得する */
    public static List<String> fetchFavoriteHorseKeys(String uid) throws Exception {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("favoriteHorses");
        DataSnapshot snapshot = Tasks.await(ref.get(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
        List<String> keys = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            if (child.getKey() != null) keys.add(child.getKey());
        }
        return keys;
    }

    /** 開催中の競馬場名を取得する */
    public static List<String> fetchActiveJoNames() throws Exception {
        return SupabaseManager.fetchActiveCoursesBlocking();
    }

    /**
     * 指定した馬（お気に入りのキー）の結果を、開催中の競馬場から集める。
     *
     * @return 馬のキー → 結果のリスト（開催日の新しい順）
     */
    public static Map<String, List<HorseResult>> fetchResults(Set<String> horseKeys, List<String> joNames) throws Exception {
        Map<String, List<HorseResult>> results = new HashMap<>();
        if (horseKeys.isEmpty()) return results;

        for (SupabaseManager.RaceResult row : SupabaseManager.fetchResultsForHorsesBlocking(horseKeys, joNames)) {
            String horseName = row.getHorseName();
            String horseKey = FirebaseManager.sanitizeKey(horseName);
            if (!horseKeys.contains(horseKey)) continue;

            HorseResult result = new HorseResult(
                    row.getKaisaijo() + "/" + row.getId(),
                    horseName,
                    row.getKaisaibi(),
                    row.getKaisaijo(),
                    String.valueOf(row.getRaceNo()),
                    row.getRaceTitle(),
                    row.getRank(),
                    row.getPopular());
            List<HorseResult> list = results.get(horseKey);
            if (list == null) {
                list = new ArrayList<>();
                results.put(horseKey, list);
            }
            list.add(result);
        }

        for (List<HorseResult> list : results.values()) {
            list.sort((a, b) -> {
                String da = a.kaisaibi != null ? a.kaisaibi : "";
                String db = b.kaisaibi != null ? b.kaisaibi : "";
                return db.compareTo(da);
            });
        }
        return results;
    }
}
