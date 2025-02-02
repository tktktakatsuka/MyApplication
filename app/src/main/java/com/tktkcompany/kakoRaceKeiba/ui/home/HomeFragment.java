package com.tktkcompany.kakoRaceKeiba.ui.home;

import android.os.Bundle;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentHomeBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    Button myButton;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    public static AdView bannerAdView;
    private SharedViewModel sharedViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // AdViewのインスタンスを取得、ロード
        loadBannerAd(binding);

        //昇順で表示
        List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();


        // 今日の日付を取得
        // 日付フォーマット（yyyyMMdd）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 現在の日付
        LocalDate today = LocalDate.now();
        String sToday = today.format(formatter);
        String youbi = WeekendDays.getDayOfWeek(sToday);

        TextView textView = binding.kaisai;
        textView.setText(sToday + youbi + "の開催情報");
        getKaisaiWetherInfo(sToday);

        RecyclerView recyclerView2 = binding.recyclerView2;
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView2.setLayoutManager(layoutManager2);

//         データのリストを準備
        List<String> data = new ArrayList<>();
        data.add("本日の馬場状態");
        data.add("レースカレンダー");
        data.add("JRAのYoutubeサイト");
//         アダプターを設定
        HorizontalAdapter adapter = new HorizontalAdapter(getContext(), data);
        recyclerView2.setAdapter(adapter);

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

            ;
        });
    }


    //バナーを表示するメソッド
    private void loadBannerAd(FragmentHomeBinding binding) {
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