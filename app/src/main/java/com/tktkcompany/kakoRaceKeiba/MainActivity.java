package com.tktkcompany.kakoRaceKeiba;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.fragment.app.Fragment;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tktkcompany.kakoRaceKeiba.databinding.ActivityMainBinding;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd interstitialAd;
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private static final String DEFAULT_CHANNEL_ID = "default_channel";
    private static final String DEFAULT_CHANNEL_NAME = "Default Notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(
                () -> {
                    MobileAds.initialize(this, initializationStatus -> {
                        Log.d(TAG, "AdMob initialized");
                    });
                })
                .start();

        // ThreeTenABPの初期化
        AndroidThreeTen.init(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_raceResults, R.id.navigation_memoLists, R.id.navigation_memoCreate, R.id.navigation_memoDetail, R.id.navigation_memoEdit, R.id.navigation_chat )
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

                    // サーバーにトークンを送信する場合はここで処理
                });


    }


    // インタースティシャル広告を表示するメソッド
    private void loadInterstitialAd() {
        String adUnitId = "ca-app-pub-4855274440005459/4078939329";


        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
                Log.d(TAG, "Interstitial ad loaded.");
                showInterstitialAd(); // ロード完了後に表示
            }

            @Override
            public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError adError) {
                interstitialAd = null;
                Log.d(TAG, "Failed to load interstitial ad: " + adError.getMessage());
            }
        });
    }

    private void showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd.show(this);
            Log.d(TAG, "Interstitial ad ready.");
        } else {
            Log.d(TAG, "Interstitial ad not ready.");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }
}