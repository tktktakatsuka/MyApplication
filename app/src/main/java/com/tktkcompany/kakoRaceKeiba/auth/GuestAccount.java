package com.tktkcompany.kakoRaceKeiba.auth;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * ログインしなくても、お気に入り馬・メモ・予想・買い目を使えるようにする。
 * 初めて開いたときに Firebase の匿名アカウントで入り、データは users/{匿名の uid} に保存する。
 * あとで Google でログインしたら、匿名アカウントを Google アカウントにつなぎ替えて、データをそのまま引き継ぐ。
 * その Google アカウントが別の端末などですでに使われていたら、匿名のデータをその Google アカウントのデータに足す。
 */
public final class GuestAccount {

    private static final String TAG = "GuestAccount";

    public interface Callback {
        /** 終わったときの今のアカウント（失敗したら null のこともある） */
        void onDone(@Nullable FirebaseUser user, boolean success);
    }

    private GuestAccount() {
    }

    /** Google でログインしていれば true（匿名で使っているだけなら false） */
    public static boolean isGoogle(@Nullable FirebaseUser user) {
        return user != null && !user.isAnonymous();
    }

    /** 表示名。匿名アカウントをつなぎ替えた直後は空のことがあるので、Google の表示名を使う */
    public static String displayName(FirebaseUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) return user.getDisplayName();
        for (com.google.firebase.auth.UserInfo info : user.getProviderData()) {
            if (info.getDisplayName() != null && !info.getDisplayName().isEmpty()) return info.getDisplayName();
        }
        return user.getEmail() != null ? user.getEmail() : "";
    }

    /** ログインしていなければ匿名で入る（すでに入っていれば何もしない） */
    public static void ensure(@Nullable Callback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            if (callback != null) callback.onDone(auth.getCurrentUser(), true);
            return;
        }
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) Log.w(TAG, "匿名でのログインに失敗", task.getException());
            if (callback != null) callback.onDone(auth.getCurrentUser(), task.isSuccessful());
        });
    }

    /** Google のアカウントでログインする。匿名で使っていたデータは引き継ぐ */
    public static void signIn(AuthCredential credential, Callback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser current = auth.getCurrentUser();
        if (current == null || !current.isAnonymous()) {
            auth.signInWithCredential(credential).addOnCompleteListener(task ->
                    callback.onDone(auth.getCurrentUser(), task.isSuccessful()));
            return;
        }
        // 匿名アカウントを Google アカウントにつなぐと、uid が変わらないのでデータもそのまま
        current.linkWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onDone(auth.getCurrentUser(), true);
            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                moveToExistingAccount(current.getUid(), credential, callback);
            } else {
                Log.w(TAG, "Google アカウントへのつなぎ替えに失敗", task.getException());
                callback.onDone(auth.getCurrentUser(), false);
            }
        });
    }

    /** すでにある Google アカウントでログインし、匿名のときのデータを足す（同じ馬・同じレースは匿名のほうで上書き） */
    private static void moveToExistingAccount(String guestUid, AuthCredential credential, Callback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        users.child(guestUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot guestData) {
                auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (!task.isSuccessful() || user == null) {
                        callback.onDone(user, false);
                        return;
                    }
                    Map<String, Object> merged = flatten(guestData);
                    if (merged.isEmpty()) {
                        callback.onDone(user, true);
                        return;
                    }
                    users.child(user.getUid()).updateChildren(merged).addOnCompleteListener(write -> {
                        if (!write.isSuccessful()) Log.w(TAG, "匿名のときのデータの引き継ぎに失敗", write.getException());
                        callback.onDone(user, true);
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "匿名のときのデータを読めませんでした: " + error.getMessage());
                auth.signInWithCredential(credential).addOnCompleteListener(task ->
                        callback.onDone(auth.getCurrentUser(), task.isSuccessful()));
            }
        });
    }

    /**
     * users/{uid} の下を「favoriteHorses/馬名」「predictions/日付/レース」のように2段下までの道にして並べる。
     * 道ごとに書き込むので、Google アカウントにある別の日付・別の馬のデータは消えない
     */
    static Map<String, Object> flatten(DataSnapshot root) {
        Map<String, Object> paths = new HashMap<>();
        for (DataSnapshot kind : root.getChildren()) {
            for (DataSnapshot first : kind.getChildren()) {
                boolean nested = first.hasChildren();
                for (DataSnapshot second : first.getChildren()) {
                    if (!second.hasChildren()) {
                        nested = false;
                        break;
                    }
                }
                if (nested) {
                    for (DataSnapshot second : first.getChildren()) {
                        paths.put(kind.getKey() + "/" + first.getKey() + "/" + second.getKey(), second.getValue());
                    }
                } else {
                    paths.put(kind.getKey() + "/" + first.getKey(), first.getValue());
                }
            }
        }
        return paths;
    }
}
