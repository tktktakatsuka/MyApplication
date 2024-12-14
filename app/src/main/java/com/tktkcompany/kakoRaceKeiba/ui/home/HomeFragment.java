package com.tktkcompany.kakoRaceKeiba.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    static MyDatabaseManager dbManager;
    private static final String PREFS_NAME = "app_preferences";
    private ProgressBar progressBar;
    private DialogLayoutBinding dialogLayoutbinding;
    private static final String KEY_LAST_CLICK_DATE = "last_click_date";
    private static final String KEY_IS_BUTTON_ENABLED = "is_button_enabled";
    Button myButton;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // ボタンの参照を取得
        myButton = root.findViewById(R.id.search_button);

        //DB接続
        dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

        // SharedPreferencesを取得
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastClickDate = sharedPreferences.getString(KEY_LAST_CLICK_DATE, null);
        boolean isButtonEnabled;

        // 現在の日付をフォーマット
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // ボタンの有効化チェック
        if (lastClickDate != null && lastClickDate.equals(today.format(formatter))) {
            isButtonEnabled = false; // 今日すでに押されている
        } else {
            isButtonEnabled = true; // ボタンを押せる
        }


        // ボタンの状態を設定
        myButton.setEnabled(isButtonEnabled);

        // 固定スレッドプールを作成
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> futures = new ArrayList<>();

        // ボタンのクリックイベントを設定
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ボタンを無効化
                myButton.setEnabled(false);
                sharedPreferences.edit()
                        .putString(KEY_LAST_CLICK_DATE, today.format(formatter))
                        .putBoolean(KEY_IS_BUTTON_ENABLED, false) // ボタンを無効に設定
                        .apply();

                //モーダルの設定
                dialogLayoutbinding = DialogLayoutBinding.inflate(inflater, container, false);
                View dialogLayoutRoot = dialogLayoutbinding.getRoot();
                MyDialogFragment dialogFragment = new MyDialogFragment();
                progressBar = dialogLayoutRoot.findViewById(R.id.progressBar);
                dialogFragment.setCancelable(false); // ダイアログ外のタッチで閉じない
                progressBar.setVisibility(View.VISIBLE);
                dialogFragment.show(requireActivity().getSupportFragmentManager(), "MyDialogFragment");

                //スレッドでスクレイピングを実施
                new Thread(() -> {
                    // それぞれのタスクを実行し、完了を待機
                    for (int i = 0; i < 6; i++) {
                        Future<String> future;
                        if (i == 0) {
                            future = executorService.submit(new Task("東京"));
                        } else if (i == 1) {
                            future = executorService.submit(new Task("京都"));
                        } else if (i == 2) {
                            future = executorService.submit(new Task("新潟"));
                        } else if (i == 3) {
                            future = executorService.submit(new Task("中京"));
                        }  else if (i == 4) {
                            future = executorService.submit(new Task("中山"));
                        }else {
                            future = executorService.submit(new Task("福島"));
                        }


                        try {
                            // タスクが完了するまで待機し、結果を取得
                            String result = future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    // すべてのタスクが完了したUIを設定
                    progressBar.setVisibility(View.GONE);
                    // 読み込み完了メッセージをToastで表示
                    dialogFragment.dismiss();

                }).

                        start();
            }
        });

        // 24:00の時刻チェック（毎回のアプリ起動時にチェック）
        checkResetButton(sharedPreferences);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void scrapingAndInsert(MyDatabaseManager dbManager, String date, String kaisaijo) {

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
                        jocode = "7";
                        break;
                    case "中京":
                        jocode = "3";
                        break;

                }
                // Webページを取得
                Document doc;
                if (raceSuu < 10) {
                    doc = Jsoup.connect("https://www.keibalab.jp/db/race/" + date + "0" + jocode + "0" + raceSuu + "/").get();
                } else {
                    doc = Jsoup.connect("https://www.keibalab.jp/db/race/" + date + "0" + jocode + raceSuu + "/").get();
                }
                // クラス名が"content"の要素を抽出
                Elements contentElements = doc.getElementsByClass("resulttable").select("tbody").select("td");
                String raceTitle = doc.getElementsByClass("raceTitle").text();
                String hassouTime = doc.getElementsByClass("classCourseSyokin").text();

                // カットする基準となる文字列
                String cutAfter = "発走";

                int j = 0;
                for (int i = 0; i < 5; i++) {
                    if (!contentElements.isEmpty()) {
                        // DB処理　データをインサート
                        dbManager.raceResultInsertData(date, kaisaijo, String.valueOf(raceSuu), contentElements.get(j).text(), contentElements.get(j + 1).text(), contentElements.get(j + 2).text(), contentElements.get(j + 3).text(), contentElements.get(j + 4).text(), contentElements.get(j + 5).text(), contentElements.get(j + 6).text(), contentElements.get(j + 7).text(), contentElements.get(j + 8).text(), contentElements.get(j + 9).text(), contentElements.get(j + 10).text(), contentElements.get(j + 11).text(), contentElements.get(j + 12).text(), contentElements.get(j + 13).text(), contentElements.get(j + 14).text(), raceTitle, hassouTime);
                    }
                    j = j + 15;
                    //会員登録分の情報をskip
                    if (i == 0) {
                        j = j + 1;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private class Task implements Callable<String> {

        String location;

        // コンストラクタで引数を受け取る
        public Task(String location) {
            this.location = location;
        }

        @Override
        public String call() {
            // タスクの処理内容
            List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();

            HomeFragment homeFragment = new HomeFragment();

            for (String date : dateList) {
                if (dbManager.getRaceResults(date, "12", location).isEmpty()) {
                    homeFragment.scrapingAndInsert(dbManager, date, location);
                }
            }
            return "Task completed by: " + Thread.currentThread().getName();
        }

    }


    private void checkResetButton(SharedPreferences sharedPreferences) {
        // 現在の日付
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 最後のクリック日を取得
        String lastClickDate = sharedPreferences.getString(KEY_LAST_CLICK_DATE, null);
        if (lastClickDate != null && !lastClickDate.equals(today.format(formatter))) {
            // 昨日以前の日付であればボタンを有効にする
            myButton.setEnabled(true);
            sharedPreferences.edit()
                    .putBoolean(KEY_IS_BUTTON_ENABLED, true) // ボタンを有効に設定
                    .apply();
        }
    }


}