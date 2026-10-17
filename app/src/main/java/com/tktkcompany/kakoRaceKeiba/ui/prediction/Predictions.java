package com.tktkcompany.kakoRaceKeiba.ui.prediction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自分の予想（◎○▲△の印）。ログイン中のユーザーのもの。
 * Firebase の users/{uid}/predictions/{開催日}/{競馬場}_{レース番号} に
 * {title: レース名, marks: {馬名: 印}} を保存する。馬名は印を外した名前をキーにする。
 */
public final class Predictions {

    /** 印の順番（タップするたびにこの順に変わり、最後の次は印なし） */
    public static final String[] MARKS = {"◎", "○", "▲", "△"};

    /** 1レース分の予想 */
    public static final class Race {
        public final String date;
        public final String course;
        public final int raceNo;
        @Nullable public final String title;
        /** 馬名（{@link FavoriteHorseNames#normalize} したもの）→ 印 */
        public final Map<String, String> marks;

        public Race(String date, String course, int raceNo, @Nullable String title, Map<String, String> marks) {
            this.date = date;
            this.course = course;
            this.raceNo = raceNo;
            this.title = title;
            this.marks = marks;
        }

        /** ◎の馬。無ければ null */
        @Nullable
        public String honmei() {
            for (Map.Entry<String, String> e : marks.entrySet()) {
                if ("◎".equals(e.getValue())) return e.getKey();
            }
            return null;
        }
    }

    public interface Callback {
        void onLoaded(List<Race> races);
    }

    public interface RaceCallback {
        void onLoaded(Map<String, String> marks);
    }

    private Predictions() {
    }

    /** 次の印。「◎」→「○」→「▲」→「△」→ null（印なし）→「◎」 */
    @Nullable
    public static String nextMark(@Nullable String current) {
        if (current == null) return MARKS[0];
        for (int i = 0; i < MARKS.length - 1; i++) {
            if (MARKS[i].equals(current)) return MARKS[i + 1];
        }
        return null;
    }

    /** ◎○▲は1レースに1頭だけ（△は何頭でもよい） */
    static boolean isUnique(String mark) {
        return !"△".equals(mark);
    }

    /**
     * 印を付ける・外す。◎○▲を付けたら、同じ印が付いていたほかの馬からは外す。
     * marks（normalize した馬名 → 印）は画面に反映できるよう書き換える
     */
    public static void setMark(String date, String course, int raceNo, @Nullable String title,
                               Map<String, String> marks, String horseName, @Nullable String mark) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference race = ref(user.getUid()).child(date).child(course + "_" + raceNo);
        String own = FavoriteHorseNames.normalize(horseName);
        if (mark != null && isUnique(mark)) {
            for (Map.Entry<String, String> e : new ArrayList<>(marks.entrySet())) {
                if (mark.equals(e.getValue()) && !e.getKey().equals(own)) {
                    marks.remove(e.getKey());
                    race.child("marks").child(e.getKey()).removeValue();
                }
            }
        }
        String key = FavoriteHorseNames.normalize(horseName);
        if (mark == null) {
            marks.remove(key);
            race.child("marks").child(key).removeValue();
        } else {
            marks.put(key, mark);
            race.child("marks").child(key).setValue(mark);
            if (title != null) race.child("title").setValue(title);
        }
    }

    /** 1レース分の印を読む（馬名 → 印。馬名は印を外したもの） */
    public static void loadRace(String date, String course, int raceNo, RaceCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onLoaded(new LinkedHashMap<>());
            return;
        }
        ref(user.getUid()).child(date).child(course + "_" + raceNo).child("marks")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, String> marks = new LinkedHashMap<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String mark = child.getValue(String.class);
                            if (child.getKey() != null && mark != null) marks.put(child.getKey(), mark);
                        }
                        callback.onLoaded(marks);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onLoaded(new LinkedHashMap<>());
                    }
                });
    }

    /** 予想したレースをすべて読む（開催日の新しい順） */
    public static void loadAll(Callback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onLoaded(Collections.emptyList());
            return;
        }
        ref(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Race> races = new ArrayList<>();
                for (DataSnapshot day : snapshot.getChildren()) {
                    for (DataSnapshot race : day.getChildren()) {
                        Race parsed = parse(day.getKey(), race);
                        if (parsed != null && !parsed.marks.isEmpty()) races.add(parsed);
                    }
                }
                races.sort((a, b) -> {
                    int c = b.date.compareTo(a.date);
                    if (c != 0) return c;
                    c = a.course.compareTo(b.course);
                    return c != 0 ? c : Integer.compare(a.raceNo, b.raceNo);
                });
                callback.onLoaded(races);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onLoaded(Collections.emptyList());
            }
        });
    }

    @Nullable
    private static Race parse(@Nullable String date, DataSnapshot race) {
        String key = race.getKey();
        if (date == null || key == null) return null;
        int sep = key.lastIndexOf('_');
        if (sep <= 0) return null;
        int raceNo;
        try {
            raceNo = Integer.parseInt(key.substring(sep + 1));
        } catch (NumberFormatException e) {
            return null;
        }
        Map<String, String> marks = new LinkedHashMap<>();
        for (DataSnapshot child : race.child("marks").getChildren()) {
            String mark = child.getValue(String.class);
            if (child.getKey() != null && mark != null) marks.put(child.getKey(), mark);
        }
        return new Race(date, key.substring(0, sep), raceNo, race.child("title").getValue(String.class), marks);
    }

    private static DatabaseReference ref(String uid) {
        return FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("predictions");
    }
}
