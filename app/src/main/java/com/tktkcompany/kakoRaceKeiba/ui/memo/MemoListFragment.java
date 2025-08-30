package com.tktkcompany.kakoRaceKeiba.ui.memo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.tktkcompany.kakoRaceKeiba.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentHomeBinding;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentMemoListBinding;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentRaceresultsBinding;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.home.HorizontalAdapter;
import com.tktkcompany.kakoRaceKeiba.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoListFragment extends Fragment {
    private MyDatabaseManager databaseManager;
    private List<Memo> memoList = new ArrayList<>();
    private MemoAdapter adapter;
    private FragmentMemoListBinding binding;
    private AdView bannerAdView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMemoListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // RecyclerViewのセットアップ
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // データベースマネージャーのインスタンス作成
        databaseManager = new MyDatabaseManager(getContext());
        databaseManager.open();
        // サンプルデータの取得
        List<Memo> list = databaseManager.getMemo();

        // サンプルデータ
        memoList.clear();  // 追加する前にリストをクリア
        for (int i = 0; i < list.size(); i++) {
            memoList.add(list.get(i));
        }

        // アダプターを設定
        adapter = new MemoAdapter(memoList, databaseManager, memo -> {
        });
        recyclerView.setAdapter(adapter);

        // 新規メモ作成ボタン
        Button createButton = root.findViewById(R.id.button_create_memo);
        createButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = null;
            navController.navigate(R.id.action_navigation_memoLists_to_navigation_memoCreate, bundle);
        });

        RecyclerView recyclerView2 = binding.recyclerView2;
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView2.setLayoutManager(layoutManager2);

        loadBannerAd();

        return root;
    }

    //バナーを表示するメソッド
    public void loadBannerAd() { // 引数から binding を削除
        AdView adView = binding.adView; // ローカル変数にした方が良い場合もある
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
            }

            @Override
            public void onAdOpened() {
                // 広告が開かれたときの処理
            }

            @Override
            public void onAdClicked() {
                // 広告がクリックされたときの処理
            }

            @Override
            public void onAdClosed() {
                // 広告が閉じられたときの処理 (ユーザーが広告からアプリに戻ったときなど)
            }
        });

        adView.loadAd(adRequest);
    }
}
