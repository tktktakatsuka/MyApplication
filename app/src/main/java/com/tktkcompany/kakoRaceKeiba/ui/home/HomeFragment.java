package com.tktkcompany.kakoRaceKeiba.ui.home;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentHomeBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;
import com.tktkcompany.kakoRaceKeiba.ui.dialog.WebViewDialogFragment;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;

import java.util.List;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;




public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public static AdView bannerAdView;
    private SharedViewModel sharedViewModel;
    private FirebaseAuth mAuth; // mAuthをメンバー変数として追加

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // ★★★ Firebase Authを初期化 ★★★
        mAuth = FirebaseAuth.getInstance();


        // AdViewのインスタンスを取得、ロード
        loadBannerAd(binding);


        // 今日の日付を取得
        // 日付フォーマット（yyyyMMdd）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 現在の日付
        LocalDate today = LocalDate.now();
        String sToday = today.format(formatter);
        String dayOfWeek = WeekendDays.getDayOfWeek(sToday);

        TextView textView = binding.kaisai;
        String formattedKaisaiInfo = getString(R.string.kaisai_info_format, sToday, dayOfWeek); // 修正後
        textView.setText(formattedKaisaiInfo);
        getKaisaiWetherInfo(sToday);

        // Firebaseから開催中の競馬場リストを取得
        FirebaseManager.queryDataAddWhere("racecourses", "isActive", "true", new ValueEventListener() {
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

        // レイアウトファイルからボタンのインスタンスを取得
        Button showWebViewDialogButton = root.findViewById(R.id.showWebViewDialogButton);

        // ボタンにクリックリスナーを設定
        showWebViewDialogButton.setOnClickListener(v -> {
            // ダイアログに表示したいURLを指定
            String url = "https://www.jra.go.jp/"; // 表示したいURLに変更してください

            // WebViewDialogFragmentのインスタンスを生成
            WebViewDialogFragment webViewDialogFragment = WebViewDialogFragment.newInstance(url);

            // DialogFragmentを表示
            // getParentFragmentManager() を使い、ActivityのFragmentManager経由で表示します
            webViewDialogFragment.show(getParentFragmentManager(), "webview_dialog");

        });

        // (もしViewModelなど他の初期化コードがあれば、それはそのまま残してください)
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void getKaisaiWetherInfo(String kaisaibi) {
        FirebaseManager.queryDataAddWhere("raceTemp", "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CardItem> itemList = new ArrayList<>();
                RecyclerView recyclerView = binding.recyclerView;
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

                List<String> kaisaiList = new ArrayList<>();
                // ViewModelを共有
                sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sCityName = childSnapshot.child("cityName").getValue(String.class);
                    String sDescription = childSnapshot.child("description").getValue(String.class);
                    String isKaisai = childSnapshot.child("isKaisai").getValue(String.class);
                    String sTemp = childSnapshot.child("temp").getValue(String.class);

                    int icon = 0;
                    if (sDescription.contains("曇") || sDescription.contains("雲")) {
                        icon = R.drawable.kumoriicon;
                    } else if (sDescription.contains("晴")) {
                        icon = R.drawable.sunnyicon;
                    } else if (sDescription.contains("雨")) {
                        icon = R.drawable.ameicon;
                    } else if (sDescription.contains("雪")) {
                        icon = R.drawable.ameicon;
                    }

                    if ("true".equals(isKaisai)) {
                        kaisaiList.add(sCityName);
                        itemList.add(new CardItem(sCityName + "競馬場の天気", sTemp + "°C", icon));
                        CardAdapter adapter = new CardAdapter(getContext(), itemList);
                        recyclerView.setAdapter(adapter);
                    }
                }

                // 開催データをセット
                sharedViewModel.setSharedData(kaisaiList);

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ★★★ 自分のボタンのクリックイベントは自分で処理する ★★★
        binding.googleSignInButton.setOnClickListener(v -> {
            // 実際のログイン処理は親のMainActivityに依頼する
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signInWithGoogle();
            }
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
            binding.aichat.setText("AIチャットを利用することが可能です");

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


}