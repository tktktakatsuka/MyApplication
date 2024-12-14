package com.tktkcompany.kakoRaceKeiba;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.ads.AdListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.tktkcompany.kakoRaceKeiba.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.tktkcompany.kakoRaceKeiba.databinding.ActivityMainBinding;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd interstitialAd;
    private static final String TAG = "MainActivity";
    private AdView bannerAdView;
    private ActivityMainBinding binding;
//    FirebaseCrashlytics crashlytics;

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

        // AdViewのインスタンスを取得、ロード
        loadBannerAd();

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_raceResults)
                .build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);



        // インタースティシャル広告の読み込み
        AdRequest adRequest = new AdRequest.Builder().build();
        loadInterstitialAd();
    }

    //バナーを表示するメソッド
    private void loadBannerAd() {
        bannerAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.d(TAG, "Failed to load banner ad: " + adError.getMessage());
            }

            @Override
            public void onAdOpened() {
                Log.d(TAG, "Banner ad opened.");
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Banner ad clicked.");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Banner ad closed.");
            }
        });

        bannerAdView.loadAd(adRequest);
    }

    // インタースティシャル広告を表示するメソッド
    private void loadInterstitialAd() {
        String adUnitId = "ca-app-pub-4855274440005459/4078939329";
//        テスト用広告ユニットID　ca-app-pub-3940256099942544/1033173712

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
//                crashlytics.setCustomKey(TAG, adError.getMessage()); // カスタム情報をセット
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
}