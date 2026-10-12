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
 * お気に入り馬の結果を、開催中の競馬場の raceResult から探す。
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
        DataSnapshot snapshot = Tasks.await(
                FirebaseDatabase.getInstance().getReference("racePickUp")
                        .orderByChild("isActive").equalTo(true).get(),
                TIMEOUT_SECONDS, TimeUnit.SECONDS);
        List<String> names = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            String name = child.child("name").getValue(String.class);
            if (name != null) names.add(name);
        }
        return names;
    }

    /**
     * 指定した馬（お気に入りのキー）の結果を、開催中の競馬場から集める。
     * 競馬場ごとに1回だけ読み込み、端末側で絞り込む。
     *
     * @return 馬のキー → 結果のリスト（開催日の新しい順）
     */
    public static Map<String, List<HorseResult>> fetchResults(Set<String> horseKeys, List<String> joNames) throws Exception {
        Map<String, List<HorseResult>> results = new HashMap<>();
        if (horseKeys.isEmpty()) return results;

        DatabaseReference root = FirebaseDatabase.getInstance().getReference("raceResult");
        for (String joName : joNames) {
            DataSnapshot snapshot = Tasks.await(root.child(joName).get(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
            for (DataSnapshot snap : snapshot.getChildren()) {
                String horseName = snap.child("horseName").getValue(String.class);
                String horseKey = FirebaseManager.sanitizeKey(horseName);
                if (!horseKeys.contains(horseKey)) continue;

                HorseResult result = new HorseResult(
                        joName + "/" + snap.getKey(),
                        horseName,
                        snap.child("kaisaibi").getValue(String.class),
                        snap.child("kaisaijo").getValue(String.class),
                        snap.child("raceNo").getValue(String.class),
                        snap.child("raceTitle").getValue(String.class),
                        snap.child("tyaku").getValue(String.class),
                        snap.child("popular").getValue(String.class));
                List<HorseResult> list = results.get(horseKey);
                if (list == null) {
                    list = new ArrayList<>();
                    results.put(horseKey, list);
                }
                list.add(result);
            }
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
