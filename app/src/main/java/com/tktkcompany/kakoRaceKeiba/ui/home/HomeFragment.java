package com.tktkcompany.kakoRaceKeiba.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;
import com.tktkcompany.kakoRaceKeiba.databinding.DialogLayoutBinding;
import com.tktkcompany.kakoRaceKeiba.R;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tktkcompany.kakoRaceKeiba.databinding.FragmentHomeBinding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.os.Handler;
import android.os.Looper;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    static MyDatabaseManager dbManager;
    private static final String PREFS_NAME = "app_preferences";
    private ProgressBar progressBar;
    private DialogLayoutBinding dialogLayoutbinding;
    private static final String KEY_LAST_CLICK_DATE = "last_click_date";
    private static final String KEY_IS_BUTTON_ENABLED = "is_button_enabled";
    Button myButton;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // ボタンの参照を取得
//        myButton = root.findViewById(R.id.search_button);
//
//        //DB接続
//        dbManager = new MyDatabaseManager(getContext());
//        dbManager.open();
//
//        // 現在の日付をフォーマット
//        LocalDate today = LocalDate.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // ボタンのクリックイベントを設定
//        myButton.setOnClickListener(v -> {
//            for (int i = 0; i < 6; i++) {
//                List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();
//                HomeFragment homeFragment = new HomeFragment();
//                if (i == 0) {
//                    // タスクの処理内容
//                    for (String date : dateList) {
//                        homeFragment.scrapingAndInsert(dbManager, date, "東京");
//                    }
//                } else if (i == 1) {
//                    for (String date : dateList) {
//                        homeFragment.scrapingAndInsert(dbManager, date, "京都");
//                    }
//                } else if (i == 2) {
//                    for (String date : dateList) {
//                        homeFragment.scrapingAndInsert(dbManager, date, "新潟");
//                    }
//                } else if (i == 3) {
//                    for (String date : dateList) {
//                        homeFragment.scrapingAndInsert(dbManager, date, "中京");
//                    }
//                } else if (i == 4) {
//                    for (String date : dateList) {
//                        homeFragment.scrapingAndInsert(dbManager, date, "中山");
//                    }
//                } else {
//                    for (String date : dateList) {
//                        homeFragment.scrapingAndInsert(dbManager, date, "福島");
//                    }
//                }
//            }
//        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void scrapingAndInsert(MyDatabaseManager dbManager, String date, String kaisaijo) {
        executorService.execute(() -> {
            try {
                for (int raceSuu = 1; raceSuu < 13; raceSuu++) {
                    String jocode = "";
                    switch (kaisaijo) {
                        case "東京":
                            jocode = "5";
                            break;
                        case "京都":
                            jocode = "8";
                            break;
                        case "新潟":
                            jocode = "4";
                            break;
                        case "福島":
                            jocode = "3";
                            break;
                        case "中山":
                            jocode = "6";
                            break;
                        case "中京":
                            jocode = "7";
                            break;

                    }
                    // Webページを取得
                    Document doc = null;
                    String url1 = "https://www.keibalab.jp/db/race/" + date + "0" + jocode + "0" + raceSuu + "/";
                    String url2 = "https://www.keibalab.jp/db/race/" + date + "0" + jocode + raceSuu + "/";
                    if (raceSuu < 10) {
                        doc = Jsoup.connect(url1).get();
                    } else {
                        doc = Jsoup.connect(url2).get();
                    }

                    if (doc != null) {
                        // クラス名が"content"の要素を抽出
                        Elements contentElements = doc.getElementsByClass("resulttable").select("tbody").select("td");
                        String raceTitle = doc.getElementsByClass("raceTitle").text();
                        String hassouTime = doc.getElementsByClass("classCourseSyokin").text();

                        int j = 0;
                        for (int i = 0; i < 5; i++) {
                            if (!contentElements.isEmpty()) {
                                // DB処理　データをインサート
                                FirebaseManager.raceResultInsertDatatoFirebase(date, kaisaijo, String.valueOf(raceSuu), contentElements.get(j).text(), contentElements.get(j + 1).text(), contentElements.get(j + 2).text(), contentElements.get(j + 3).text(), contentElements.get(j + 4).text(), contentElements.get(j + 5).text(), contentElements.get(j + 6).text(), contentElements.get(j + 7).text(), contentElements.get(j + 8).text(), contentElements.get(j + 9).text(), contentElements.get(j + 10).text(), contentElements.get(j + 11).text(), contentElements.get(j + 12).text(), contentElements.get(j + 13).text(), contentElements.get(j + 14).text(), raceTitle, hassouTime);
                            }
                            j = j + 15;
                            //会員登録分の情報をskip
                            if (i == 0) {
                                j = j + 1;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}