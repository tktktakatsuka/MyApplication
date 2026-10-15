package com.tktkcompany.kakoRaceKeiba.ui.horsememo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 馬ごとのメモ（ログイン中のユーザーのもの）。
 * Firebase の users/{uid}/horseMemos/{馬名} に、本文・書いた場面・更新日時を保存する。
 * 馬名は「(外)」などの印を外した名前をキーにして、出走表・結果のどちらからでも同じメモを引けるようにする。
 */
public final class HorseMemos {

    public static final class Memo {
        public final String name;
        public final String text;
        /** メモを書いた場面（例: 「10/12(月) 東京3R 5着」）。無ければ null */
        @Nullable public final String from;
        public final long updatedAt;

        public Memo(String name, String text, @Nullable String from, long updatedAt) {
            this.name = name;
            this.text = text;
            this.from = from;
            this.updatedAt = updatedAt;
        }
    }

    public interface Callback {
        void onLoaded(Map<String, Memo> memos);
    }

    private HorseMemos() {
    }

    public static boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /** メモを1回だけ読み込む。キーは印を外した馬名。未ログインなら空 */
    public static void load(Callback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onLoaded(Collections.emptyMap());
            return;
        }
        ref(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Memo> memos = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String text = child.child("text").getValue(String.class);
                    if (child.getKey() == null || text == null || text.trim().isEmpty()) continue;
                    String name = child.child("name").getValue(String.class);
                    Long updatedAt = child.child("updatedAt").getValue(Long.class);
                    memos.put(child.getKey(), new Memo(name != null ? name : child.getKey(), text,
                            child.child("from").getValue(String.class), updatedAt != null ? updatedAt : 0L));
                }
                callback.onLoaded(memos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onLoaded(Collections.emptyMap());
            }
        });
    }

    @Nullable
    public static Memo find(Map<String, Memo> memos, String horseName) {
        return memos.get(FavoriteHorseNames.normalize(horseName));
    }

    /** 保存して、画面に反映するための Memo を返す。本文が空なら削除して null を返す */
    @Nullable
    public static Memo save(Map<String, Memo> memos, String horseName, String text, @Nullable String from) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = FavoriteHorseNames.normalize(horseName);
        if (user == null || key.isEmpty()) return null;
        if (text == null || text.trim().isEmpty()) {
            delete(memos, horseName);
            return null;
        }
        String name = displayName(horseName);
        Map<String, Object> value = new HashMap<>();
        value.put("name", name);
        value.put("text", text.trim());
        if (from != null) value.put("from", from);
        value.put("updatedAt", ServerValue.TIMESTAMP);
        ref(user.getUid()).child(key).setValue(value);

        Memo memo = new Memo(name, text.trim(), from, System.currentTimeMillis());
        memos.put(key, memo);
        return memo;
    }

    public static void delete(Map<String, Memo> memos, String horseName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = FavoriteHorseNames.normalize(horseName);
        if (user == null || key.isEmpty()) return;
        ref(user.getUid()).child(key).removeValue();
        memos.remove(key);
    }

    /** 「(外)オフトレイル」→「オフトレイル」（表示用。キーと違い記号は残す） */
    static String displayName(String horseName) {
        if (horseName == null) return "";
        return horseName.trim().replaceFirst("^[\\(（\\[［][^\\)）\\]］]{1,2}[\\)）\\]］]\\s*", "");
    }

    private static DatabaseReference ref(String uid) {
        return FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("horseMemos");
    }
}
