package com.tktkcompany.kakoRaceKeiba.ui.memo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * メモの保存先を切り替えるクラス。
 * ログイン中は Firebase（users/{uid}/memos）に保存して機種変更後も残るようにし、
 * 未ログイン時は従来どおり端末内のSQLiteに保存する。
 */
public class MemoRepository {

    private static final String TAG = "MemoRepository";

    public interface Callback<T> {
        void onResult(T result);
    }

    private final Context appContext;

    public MemoRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public boolean isCloud() {
        return currentUid() != null;
    }

    public void load(Callback<List<Memo>> callback) {
        String uid = currentUid();
        if (uid == null) {
            callback.onResult(withLocalDb(MyDatabaseManager::getMemo));
            return;
        }
        memosRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Memo> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    list.add(new Memo(child.getKey(),
                            child.child("title").getValue(String.class),
                            child.child("content").getValue(String.class)));
                }
                callback.onResult(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "メモの読み込みに失敗: " + error.getMessage());
                callback.onResult(new ArrayList<>());
            }
        });
    }

    /** 成功したら true を返す */
    public void add(String title, String content, Callback<Boolean> callback) {
        String uid = currentUid();
        if (uid == null) {
            withLocalDb(db -> db.insertMemo(title, content));
            callback.onResult(true);
            return;
        }
        memosRef(uid).push().setValue(toMap(title, content))
                .addOnCompleteListener(task -> callback.onResult(task.isSuccessful()));
    }

    public void update(Memo memo, Callback<Boolean> callback) {
        String uid = currentUid();
        if (uid == null) {
            withLocalDb(db -> {
                db.updateMemo(memo);
                return null;
            });
            callback.onResult(true);
            return;
        }
        memosRef(uid).child(memo.getId()).setValue(toMap(memo.getTitle(), memo.getContent()))
                .addOnCompleteListener(task -> callback.onResult(task.isSuccessful()));
    }

    public void delete(Memo memo) {
        String uid = currentUid();
        if (uid == null) {
            withLocalDb(db -> {
                db.deleteMemo(memo.getId());
                return null;
            });
            return;
        }
        memosRef(uid).child(memo.getId()).removeValue();
    }

    /**
     * ログイン直後に、端末内のメモをクラウドへ移す。
     * 全件の保存に成功した分だけ端末から削除する。
     */
    public static void migrateLocalToCloud(Context context, String uid, Callback<Integer> onMigrated) {
        MemoRepository repo = new MemoRepository(context);
        List<Memo> localMemos = repo.withLocalDb(MyDatabaseManager::getMemo);
        if (localMemos.isEmpty()) return;

        DatabaseReference ref = memosRef(uid);
        int[] remaining = {localMemos.size()};
        int[] migrated = {0};
        for (Memo memo : localMemos) {
            ref.push().setValue(toMap(memo.getTitle(), memo.getContent()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            repo.withLocalDb(db -> {
                                db.deleteMemo(memo.getId());
                                return null;
                            });
                            migrated[0]++;
                        } else {
                            Log.w(TAG, "メモのクラウド移行に失敗", task.getException());
                        }
                        if (--remaining[0] == 0 && migrated[0] > 0) {
                            onMigrated.onResult(migrated[0]);
                        }
                    });
        }
    }

    private interface DbAction<T> {
        T run(MyDatabaseManager db);
    }

    private <T> T withLocalDb(DbAction<T> action) {
        MyDatabaseManager db = new MyDatabaseManager(appContext);
        db.open();
        try {
            return action.run(db);
        } finally {
            db.close();
        }
    }

    private static String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private static DatabaseReference memosRef(String uid) {
        return FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("memos");
    }

    private static Map<String, Object> toMap(String title, String content) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("updatedAt", ServerValue.TIMESTAMP);
        return map;
    }
}
