package com.tktkcompany.kakoRaceKeiba;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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

    // MainActivity のフィールド
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        // CredentialManager 初期化
        credentialManager = CredentialManager.create(this);

        // ActivityResultLauncher 初期化（Google Sign-In fallback 用）
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                String idToken = account.getIdToken();
                                firebaseAuthWithGoogle(idToken); // 既存のFirebase認証処理を使う
                            }
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(this, "Googleサインインに失敗しました", Toast.LENGTH_SHORT).show();
                            updateHomeFragmentUI(null);
                        }
                    }
                }
        );

        // AdMob 初期化（別スレッドで）
        new Thread(() -> MobileAds.initialize(this, initializationStatus -> Log.d(TAG, "AdMob initialized")))
                .start();

        // ThreeTenABP の初期化
        AndroidThreeTen.init(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.navView.setPadding(0, 0, 0, systemBars.bottom);
            return windowInsets;
        });

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_raceResults,
                R.id.navigation_memoLists,
                R.id.navigation_memoCreate,
                R.id.navigation_memoDetail,
                R.id.navigation_memoEdit,
                R.id.navigation_chat
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 通知チャンネル作成
        createNotificationChannel();

        // FCM トークン取得
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
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
                this,
                request,
                null,
                getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // 取得した Credential を処理
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential
                                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            try {
                                Bundle credentialData = credential.getData();
                                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
                                String idToken = googleIdTokenCredential.getIdToken();
                                firebaseAuthWithGoogle(idToken);
                            } catch (Exception e) {
                                Log.e(TAG, "GoogleIdTokenCredential の作成に失敗", e);
                                updateHomeFragmentUI(null);
                            }
                        } else {
                            Log.w(TAG, "取得した Credential が Google ID トークンではありませんでした。");
                            Toast.makeText(MainActivity.this, "ログインに失敗しました。", Toast.LENGTH_SHORT).show();
                            updateHomeFragmentUI(null);
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e instanceof NoCredentialException) {
                            Log.w(TAG, "保存済みの認証情報がありません。GoogleサインインUIを表示します。");
                            launchGoogleSignInFallback();
                        } else {
                            Log.e(TAG, "GetCredential failed", e);
                            Toast.makeText(MainActivity.this, "ログイン処理でエラーが発生しました。", Toast.LENGTH_SHORT).show();
                        }
                        updateHomeFragmentUI(null);
                    }
                }
        );
    }

    /**
     * ログアウト処理（HomeFragment から呼ばれる）
     */
    public void signOut() {
        // Firebase からログアウト
        mAuth.signOut();

        // ViewModel の UID をクリア
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.setUid(null);

        // HomeFragment の UI を未ログイン状態に更新
        updateHomeFragmentUI(null);

        Toast.makeText(this, "ログアウトしました。", Toast.LENGTH_SHORT).show();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    // 認証完了後、HomeFragment に UI 更新を依頼
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

    private void launchGoogleSignInFallback() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * 表示されている HomeFragment を見つけて UI を更新する
     *
     * @param user ログインユーザー情報 (未ログインなら null)
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
