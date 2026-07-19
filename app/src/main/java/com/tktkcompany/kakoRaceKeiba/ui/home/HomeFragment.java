package com.tktkcompany.kakoRaceKeiba.ui.home;


import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentHomeBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.format.DateTimeFormatter;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public static AdView bannerAdView;
    private SharedViewModel sharedViewModel;
    private FirebaseAuth mAuth; // mAuthをメンバー変数として追加
    private boolean exeSetRaceResult;
    // yyyyMMdd形式用
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        View root = binding.getRoot();


        // AdViewのインスタンスを取得、ロード
        loadBannerAd(binding);

        // Firebaseから開催中の競馬場リストを取得
        FirebaseManager.queryDataAddWhere("racePickUp", "isActive", "true", new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> joNames = new ArrayList<>();

                for (DataSnapshot joSnapshot : snapshot.getChildren()) {
                    String joName = joSnapshot.child("name").getValue(String.class);
                    if (joName != null) {
                        joNames.add(joName);
                    }
                }
                sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                sharedViewModel.setJoNames(joNames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "データの読み込みに失敗しました", Toast.LENGTH_SHORT).show();
            }
        });


        // 🔑 setJoNames 完了後にここが呼ばれる
        sharedViewModel.getJoNames().observe(getViewLifecycleOwner(), joNames -> {
            if (joNames == null || joNames.isEmpty()) return;

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("raceResult");
            for (String joName : joNames) {
                dbRef.child(joName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot raceSnap : snapshot.getChildren()) {
                            String raceNo = raceSnap.child("raceNo").getValue(String.class);
                            if (!"11".equals(raceNo)) continue;
                            String raceDateStr = raceSnap.child("kaisaibi").getValue(String.class);
                            String raceTitle = raceSnap.child("raceTitle").getValue(String.class);
                            String kaisaijo = raceSnap.child("kaisaijo").getValue(String.class);
                            String horseName = raceSnap.child("horseName").getValue(String.class);
                            String tyaku = raceSnap.child("tyaku").getValue(String.class);
                            String hassouTime = raceSnap.child("hassouTime").getValue(String.class);
                            String winOdds = raceSnap.child("winOdds").getValue(String.class);

                            if ("東京".equals(kaisaijo)) {
                                raceResultSet(tyaku, raceTitle, kaisaijo, hassouTime, raceDateStr, winOdds, horseName, root);
                            } else {
                                raceResultSet2(tyaku, raceTitle, kaisaijo, hassouTime, raceDateStr, winOdds, horseName, root);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error: " + error.getMessage());
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ★★★ Firebase Authを初期化 ★★★
        mAuth = FirebaseAuth.getInstance();

        // ★★★ 自分のボタンのクリックイベントは自分で処理する ★★★
        binding.googleSignInButton.setOnClickListener(v -> {
            // 実際のログイン処理は親のMainActivityに依頼する
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signInWithGoogle();
            }
        });

        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            updateUI(currentUser);
        });

        // ★★★ ログアウトボタンのクリックリスナーを追加 ★★★
        binding.logoutButton.setOnClickListener(v -> {
            // 実際のログアウト処理は親のMainActivityに依頼する
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signOut();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // ★★★ ここからが重要な修正 ★★★
        // Fragmentが表示されるたびに、現在のログイン状態をチェックしてUIを更新する
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        // ★★★ ここまで ★★★
    }

    // ★★★ 自分の画面のUI更新は自分で行う ★★★
// (MainActivityから呼び出されるようにpublicにする)
    public void updateUI(FirebaseUser user) {
        if (binding == null) return; // 画面が破棄された後は何もしない

        if (user != null) {
            // ログイン済みの場合
            binding.userInfoText.setText("ようこそ, " + user.getDisplayName() + "さん　ログイン中です。");
            binding.userInfoText.setVisibility(View.VISIBLE);
            binding.googleSignInButton.setVisibility(View.GONE);
            binding.logoutButton.setText("ログアウト");
            binding.logoutButton.setVisibility(View.VISIBLE);

            // ★ ViewModelの初期化をここで行うとより安全
            if (sharedViewModel == null) {
                sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            }
            String userId = user.getUid();
            sharedViewModel.setUid(userId);

        } else {
            // 未ログインの場合
            binding.userInfoText.setVisibility(View.GONE);
            binding.logoutButton.setVisibility(View.GONE);
            binding.googleSignInButton.setVisibility(View.VISIBLE);

        }
    }


    //バナーを表示するメソッド
    public void loadBannerAd(FragmentHomeBinding binding) {
        bannerAdView = binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();

        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        bannerAdView.loadAd(adRequest);
    }

    private Bundle setKeyjoNameString(String date, String jo) {
        // 渡したい値を用意する
        // Bundleを作成して値を詰める
        Bundle bundle = new Bundle();
        bundle.putString("key", date);
        bundle.putString("jo", jo);
        return bundle;
    }

    private void raceResultSet(String tyaku, String raceTitle, String kaisaijo, String hassouTime, String raceDateStr, String winOdds, String horseName, View root) {

        if ("1".equals(tyaku)) {
            //レースタイトル,時刻、場名を設定する
            binding.textTodayRaceName.setText(raceTitle);
            binding.textTodayRaceLocation.setText(kaisaijo);
            binding.textTodayRaceTime.setText(hassouTime);
            binding.textTodayRaceNumber.setText(raceDateStr + kaisaijo + "のレース結果");
            binding.textPopularRaceResultMore.setText(kaisaijo + "のレース結果をもっとみる");
            binding.textRaceResult1Odds.setText(winOdds);
            //レースの着順を設定する。
            binding.textRaceResult1Name.setText(horseName);
            // ▼ ここで「もっと見る」にクリックリスナーを設定する
            TextView moreTextView = root.findViewById(R.id.text_popular_raceResult_more);
            moreTextView.setOnClickListener(v -> {
                // 例: 直近の「日付」と「競馬場」を渡して同じ画面に遷移
                String date = raceDateStr; // ← 実際には任意の date に置き換える
                String joName = kaisaijo;   // ← 実際には任意の joName に置き換える
                Bundle bundle = setKeyjoNameString(date, joName);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_raceResults, bundle);
            });

        } else if ("2".equals(tyaku)) {
            //レースの着順を設定する。
            binding.textRaceResult2Name.setText(horseName);
            binding.textRaceResult2Odds.setText(winOdds);
        } else if ("3".equals(tyaku)) {
            //レースの着順を設定する。
            binding.textRaceResult3Name.setText(horseName);
            binding.textRaceResult3Odds.setText(winOdds);
        }
    }

    private void raceResultSet2(String tyaku, String raceTitle, String kaisaijo, String hassouTime, String raceDateStr, String winOdds, String horseName, View root) {
        if ("1".equals(tyaku)) {
            //レースタイトル,時刻、場名を設定する
            binding.text2TodayRaceName.setText(raceTitle);
            binding.text2TodayRaceLocation.setText(kaisaijo);
            binding.text2TodayRaceTime.setText(hassouTime);
            binding.text2TodayRaceNumber.setText(raceDateStr + kaisaijo + "のレース結果");
            binding.text2PopularRaceResultMore.setText(kaisaijo + "のレース結果をもっとみる");
            binding.text2RaceResult1Odds.setText(winOdds);
            //レースの着順を設定する。
            binding.text2RaceResult1Name.setText(horseName);
            // ▼ ここで「もっと見る」にクリックリスナーを設定する
            TextView moreTextView = root.findViewById(R.id.text2_popular_raceResult_more);
            moreTextView.setOnClickListener(v -> {
                // 例: 直近の「日付」と「競馬場」を渡して同じ画面に遷移
                String date = raceDateStr; // ← 実際には任意の date に置き換える
                String joName = kaisaijo;   // ← 実際には任意の joName に置き換える
                Bundle bundle = setKeyjoNameString(date, joName);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_raceResults, bundle);
            });

        } else if ("2".equals(tyaku)) {
            //レースの着順を設定する。
            binding.text2RaceResult2Name.setText(horseName);
            binding.text2RaceResult2Odds.setText(winOdds);
        } else if ("3".equals(tyaku)) {
            //レースの着順を設定する。
            binding.text2RaceResult3Name.setText(horseName);
            binding.text2RaceResult3Odds.setText(winOdds);
        }
    }


}

