package com.tktkcompany.kakoRaceKeiba;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.google.android.gms.ads.MobileAds;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.tktkcompany.kakoRaceKeiba.databinding.ActivityMainBinding;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;
import com.tktkcompany.kakoRaceKeiba.ui.home.HomeFragment;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String DEFAULT_CHANNEL_ID = "default_channel";
    private static final String DEFAULT_CHANNEL_NAME = "Default Notifications";
    private CredentialManager credentialManager;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ★★★ 2. 変数を必ず初期化する ★★★
        credentialManager = CredentialManager.create(this);

        new Thread(
                () -> MobileAds.initialize(this, initializationStatus -> Log.d(TAG, "AdMob initialized")))
                .start();

        // ThreeTenABPの初期化
        AndroidThreeTen.init(this);
        com.tktkcompany.kakoRaceKeiba.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_raceResults, R.id.navigation_memoLists, R.id.navigation_memoCreate, R.id.navigation_memoDetail, R.id.navigation_memoEdit, R.id.navigation_chat)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 通知チャンネルを作成
        createNotificationChannel();
        // トークンを取得
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // トークンを取得
                    String token = task.getResult();
                    Log.d("FCM Token", token);
                });
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("This is the default notification channel.");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }


    /**
     * HomeFragmentから呼び出されるサインイン処理
     */
    public void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this, request, null, getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // ★★★ ここからが抜けていた重要な処理 ★★★
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            try {
                                // 宝箱から鍵（IDトークン）を取り出す
                                Bundle credentialData = credential.getData();
                                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
                                String idToken = googleIdTokenCredential.getIdToken();
                                // 取り出した鍵をFirebaseに渡す
                                firebaseAuthWithGoogle(idToken);
                            } catch (Exception e) {
                                Log.e(TAG, "GoogleIdTokenCredentialの作成に失敗", e);
                                updateHomeFragmentUI(null); // UIを更新
                            }
                        } else {
                            Log.w(TAG, "取得したCredentialがGoogle IDトークンではありませんでした。");
                            Toast.makeText(MainActivity.this, "ログインに失敗しました。", Toast.LENGTH_SHORT).show();
                            updateHomeFragmentUI(null); // UIを更新
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "GetCredential failed", e);
                        Toast.makeText(MainActivity.this, "ログイン処理でエラーが発生しました。", Toast.LENGTH_SHORT).show();
                        // 失敗した場合もFragmentのUIを更新する
                        updateHomeFragmentUI(null);
                    }
                }
        );
    }

    /**
     * ★★★ ログアウト処理を行うメソッド ★★★
     * HomeFragmentから呼び出されるようにpublicにする
     */
    public void signOut() {
        // 1. Firebaseからログアウト
        mAuth.signOut();
        // ★★★ ViewModelを初期化 ★★★
        // ★★★ ViewModelのインスタンスをメンバー変数として宣言 ★★★
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        // 3. ViewModelに保持しているUIDをクリアする
        sharedViewModel.setUid(null);
        // 4. HomeFragmentのUIを更新するよう依頼する (未ログイン状態を渡す)
        updateHomeFragmentUI(null);
        // 5. ユーザーにログアウトしたことを通知する
        Toast.makeText(this, "ログアウトしました。", Toast.LENGTH_SHORT).show();
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    // 認証完了後、HomeFragmentにUIの更新を依頼する
                    updateHomeFragmentUI(user);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        if (user != null) {
                            Toast.makeText(this, "ログイン成功: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Firebaseでの認証に失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 表示されているHomeFragmentを見つけて、UIの更新を依頼するメソッド
     *
     * @param user ログインユーザー情報 (未ログインならnull)
     */
    private void updateHomeFragmentUI(FirebaseUser user) {
        try {
            Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
            if (navHostFragment != null) {
                Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).updateUI(user);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to update HomeFragment UI", e);
        }
    }
}