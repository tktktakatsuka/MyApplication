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
import com.tktkcompany.kakoRaceKeiba.ui.dialog.WebViewDialogFragment;
import com.tktkcompany.kakoRaceKeiba.ui.home.HorizontalAdapter;
import com.tktkcompany.kakoRaceKeiba.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoListFragment extends Fragment {
    private MemoRepository memoRepository;
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
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ログイン中はクラウド、未ログイン時は端末に保存する
        memoRepository = new MemoRepository(requireContext());
        binding.textMemoStorage.setText(memoRepository.isCloud()
                ? "メモはアカウントに保存されます（機種変更後も引き継げます）"
                : "メモはこの端末に保存されます。ログインするとアカウントに保存できます");

        // アダプターを設定
        memoList.clear();
        adapter = new MemoAdapter(memoList, memoRepository, memo -> {
        });
        recyclerView.setAdapter(adapter);

        memoRepository.load(list -> {
            if (binding == null) return;
            memoList.clear();
            memoList.addAll(list);
            adapter.notifyDataSetChanged();
        });

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
