package com.example.myapplication.ui.home;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.db.MyDatabaseManager;
import com.example.myapplication.util.WeekendDays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //DB接続
        MyDatabaseManager dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

        //Jsoupの処理を別スレッドで実行
        new Thread(new Runnable() {
            public void run() {
                List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();
                for (String date : dateList) {
                    if (dbManager.getRaceResults(date, "1", "東京").size() == 0) {
                        scrapingAndInsert(dbManager, date, "東京");
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();
                for (String date : dateList) {
                    if (dbManager.getRaceResults(date, "1", "京都").size() == 0) {
                        scrapingAndInsert(dbManager, date, "京都");
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();
                for (String date : dateList) {
                    if (dbManager.getRaceResults(date, "1", "新潟").size() == 0) {
                        scrapingAndInsert(dbManager, date, "新潟");
                    }
                }
            }
        }).start();



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * @param dbManager
     * @param date
     * @param kaisaijo
     */
    private void scrapingAndInsert(MyDatabaseManager dbManager, String date, String kaisaijo) {

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
                Elements contentElements = doc.getElementsByClass("resulttable");
                contentElements = doc.select("tbody");
                contentElements = doc.select("td");
                String raceTitle = doc.getElementsByClass("raceTitle").text();
                String hassouTime = doc.getElementsByClass("classCourseSyokin").text();

                // 元の文字列
                String originalString = hassouTime;

                // カットする基準となる文字列
                String cutAfter = "発走";
                String result ="";

                // 指定の文字列が存在する場合にカットする
                int index = originalString.indexOf(cutAfter);
                if (index != -1) {
                    // 指定の文字列以降の部分をカット
                    result = originalString.substring(0, index + cutAfter.length());
                }

                int j = 0;
                for (int i = 0; i < 5; i++) {
                    if (contentElements.size() != 0) {
                        // DB処理　データをインサート
                        dbManager.raceResultInsertData(date, kaisaijo, String.valueOf(raceSuu), contentElements.get(j).text(),
                                contentElements.get(j + 1).text(), contentElements.get(j + 2).text(), contentElements.get(j + 3).text(),
                                contentElements.get(j + 4).text(), contentElements.get(j + 5).text(), contentElements.get(j + 6).text(),
                                contentElements.get(j + 7).text(), contentElements.get(j + 8).text(), contentElements.get(j + 9).text(),
                                contentElements.get(j + 10).text(), contentElements.get(j + 11).text(), contentElements.get(j + 12).text(),
                                contentElements.get(j + 13).text(), contentElements.get(j + 14).text(), raceTitle, originalString);
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
}