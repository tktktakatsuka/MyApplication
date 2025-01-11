package com.tktkcompany.kakoRaceKeiba.ui.dashboard;


import android.os.Bundle;


import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.DayOfWeek;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentDashboardBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseHelper;
import com.tktkcompany.kakoRaceKeiba.MainActivity;

import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {
    private MyDatabaseHelper dbHelper;
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

    private LinearLayout buttonContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ボタンを追加するLinearLayoutの参照を取得
        buttonContainer = root.findViewById(R.id.button_container);

        // 各競馬場のリストを作成
        List<String> joNames = List.of(TOKYO, NAKAYAMA, HUKUSIMA, TYUKYO, NIIGATA, KYOTO);

        // 日付のリストを取得
        List<String> datelist = WeekendDays.getPastWeekendsInCurrentMonth();
        datelist.add("20250106");

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

    public static String getDayOfWeek(String dateStr) {
        try {
            // 8桁の日付フォーマットを定義
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(dateStr, formatter);
            // 曜日を取得
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            // 日本語の曜日名を返す
            switch (dayOfWeek) {
                case MONDAY:
                    return " (月)";
                case TUESDAY:
                    return " (火)";
                case WEDNESDAY:
                    return " (水)";
                case THURSDAY:
                    return " (木)";
                case FRIDAY:
                    return " (金)";
                case SATURDAY:
                    return " (土)";
                case SUNDAY:
                    return " (日)";
                default:
                    return "";
            }
        } catch (DateTimeParseException e) {
            return "日付の形式が正しくありません。";
        }
    }


    private void executeSequentialQueriesForAllLocations(List<String> datelist, List<String> joNames) {
        // 競馬場ごとに処理を順次追加
        for (String joName : joNames) {
            getActivity().runOnUiThread(() -> {
                // 競馬場所ごとにクエリを実行
                queryDataAsTask(datelist, joName);
            });
        }
    }

    private void queryDataAsTask(List<String> dateList, String joName) {
        FirebaseManager.queryData("raceResult" + "/" + joName, "kaisaibi", "", new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 競馬場名を表示する固定テキストを追加
                TextView textView = new TextView(getActivity());
                textView.setText(joName);
                textView.setTextSize(18);
                textView.setPadding(0, 20, 0, 10); // 上下の余白を設定
                buttonContainer.addView(textView);

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNo").getValue(String.class);
                    String sTyaku = childSnapshot.child("tyaku").getValue(String.class);
                    String kaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);
                    String popular = childSnapshot.child("popular").getValue(String.class);
                    for (String date : dateList) {
                        if ("1".equals(sTyaku) && "1".equals(sRaceNo) && date.equals(kaisaibi)) {
                            createBundle(joName, kaisaibi);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void createBundle(String joName, String date) {
        getActivity().runOnUiThread(() -> {
            Button newButton = new Button(getActivity());
            newButton.setText(date + getDayOfWeek(date));
            newButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                Bundle bundle = setKeyjoNameString(date, joName);
                navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
            });
            buttonContainer.addView(newButton);
        });
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


