package com.tktkcompany.kakoRaceKeiba.ui.dashboard;


import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentDashboardBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;

import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DashboardFragment extends Fragment {

    private static AdView bannerAdView;
    private FragmentDashboardBinding binding;

    private final String TOKYO = "東京";
    private final String NAKAYAMA = "中山";
    private final String KYOTO = "京都";
    private final String HUKUSIMA = "福島";
    private final String TYUKYO = "中京";
    private final String NIIGATA = "新潟";
    private final String HANSIN = "阪神";
    private final String KOKURA = "小倉";
    private ProgressBar progressBar;

    private LinearLayout buttonContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //スピナー表示
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);

        // ボタンを追加するLinearLayoutの参照を取得
        buttonContainer = root.findViewById(R.id.button_container);

        // 各競馬場のリストを作成
        List<String> joNames = List.of(TOKYO, KYOTO, KOKURA);

        // 日付のリストを取得
        List<String> datelist = WeekendDays.getPastWeekendsInCurrentMonth();
        // 日付ごとに競馬場のクエリを順番に実行
        executeSequentialQueriesForAllLocations(datelist, joNames);

        // AdViewのインスタンスを取得、ロード
        loadBannerAd();
        return root;
    }

    private Bundle setKeyjoNameString(String date, String jo) {
        // 渡したい値を用意する
        // Bundleを作成して値を詰める
        Bundle bundle = new Bundle();
        bundle.putString("key", date);
        bundle.putString("jo", jo);
        return bundle;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void executeSequentialQueriesForAllLocations(List<String> datelist, List<String> joNames) {
        // 競馬場ごとに処理を順次追加
        for (String joName : joNames) {
            // 競馬場所ごとにクエリを実行
            queryDataAsTask(datelist, joName);
        }
    }

    private void queryDataAsTask(List<String> dateList, String joName) {
        FirebaseManager.queryData("raceResult" + "/" + joName, "kaisaibi", "", new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 競馬場名を表示する固定テキストを追加
                if (isAdded() && getActivity() != null) {
                    TextView textView = new TextView(getActivity());
                    textView.setText(joName);
                    textView.setTextSize(18);
                    textView.setPadding(0, 20, 0, 10); // 上下の余白を設定
                    buttonContainer.addView(textView);
                }

                List<DataSnapshot> dataList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    dataList.add(childSnapshot);
                }

                CheckBox checkBox = binding.checkboxDate;
                if (checkBox.isChecked()) {
                    // 降順に並び替え
                    Collections.reverse(dataList);
                    checkBox.setChecked(true);
                }

                for (DataSnapshot reserveSnapshot : dataList){
                    String sRaceNo = reserveSnapshot.child("raceNo").getValue(String.class);
                    String sTyaku = reserveSnapshot.child("tyaku").getValue(String.class);
                    String kaisaibi = reserveSnapshot.child("kaisaibi").getValue(String.class);
                    for (String date : dateList) {
                        if ("1".equals(sTyaku) && "1".equals(sRaceNo) && date.equals(kaisaibi)) {
                            createBundle(joName, kaisaibi);
                            break;
                        }
                    }
                }


                if (isAdded() && progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void createBundle(String joName, String date) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Button newButton = new Button(getActivity());
                newButton.setText(date + WeekendDays.getDayOfWeek(date));
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    Bundle bundle = setKeyjoNameString(date, joName);
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                buttonContainer.addView(newButton);
            });
        }
    }

    //バナーを表示するメソッド
    public void loadBannerAd() {
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


