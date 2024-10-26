package com.example.myapplication.ui.home;

import android.os.Bundle;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.example.myapplication.R;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.db.MyDatabaseManager;
import com.example.myapplication.util.WeekendDays;

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

    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressBar = root.findViewById(R.id.progressBar);
        // 処理開始時にスピナー表示
        showLoadingSpinner();

        //DB接続
        dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

        // 固定スレッドプールを作成
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<String>> futures = new ArrayList<>();

        List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();

        // タスクをサブミットし、Futureオブジェクトをリストに追加
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                futures.add(executorService.submit(new Task("東京")));
            } else if (i == 1) {
                futures.add(executorService.submit(new Task("京都")));
            } else if (i == 2) {
                futures.add(executorService.submit(new Task("新潟")));
            }
        }

        // すべてのタスクの完了を待機
        for (Future<String> future : futures) {
            try {
                // タスクが完了するまで待機し、結果を取得
                String result = future.get();
                dbManager.executerInsertData("1");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        // ExecutorServiceの終了
        executorService.shutdown();

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
                if (kaisaijo.equals("東京")) {
                    jocode = "5";
                } else if (kaisaijo.equals("京都")) {
                    jocode = "8";
                } else if (kaisaijo.equals("新潟")) {
                    jocode = "4";
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
                String result = "";

                // 指定の文字列が存在する場合にカットする
                int index = hassouTime.indexOf(cutAfter);
                if (index != -1) {
                    // 指定の文字列以降の部分をカット
                    result = hassouTime.substring(0, index + cutAfter.length());
                }

                int j = 0;
                for (int i = 0; i < 5; i++) {
                    if (!contentElements.isEmpty()) {
                        // DB処理　データをインサート
                        dbManager.raceResultInsertData(date, kaisaijo, String.valueOf(raceSuu), contentElements.get(j).text(),
                                contentElements.get(j + 1).text(), contentElements.get(j + 2).text(), contentElements.get(j + 3).text(),
                                contentElements.get(j + 4).text(), contentElements.get(j + 5).text(), contentElements.get(j + 6).text(),
                                contentElements.get(j + 7).text(), contentElements.get(j + 8).text(), contentElements.get(j + 9).text(),
                                contentElements.get(j + 10).text(), contentElements.get(j + 11).text(), contentElements.get(j + 12).text(),
                                contentElements.get(j + 13).text(), contentElements.get(j + 14).text(), raceTitle, hassouTime);
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
        public String call() throws Exception {
            // タスクの処理内容
            List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();

            HomeFragment homeFragment = new HomeFragment();

            for (String date : dateList) {
                if (dbManager.getRaceResults(date, "1", location).isEmpty()) {
                    homeFragment.scrapingAndInsert(dbManager, date, location);
                }
            }

            // 処理完了後、メインスレッドでスピナーを非表示にする
            hideLoadingSpinner();
            return "Task completed by: " + Thread.currentThread().getName();
        }

    }

    // スピナー表示メソッド
    private void showLoadingSpinner() {
        progressBar.setVisibility(View.VISIBLE);
    }

    // スピナー非表示メソッド
    private void hideLoadingSpinner() {
        progressBar.setVisibility(View.GONE);
    }
}