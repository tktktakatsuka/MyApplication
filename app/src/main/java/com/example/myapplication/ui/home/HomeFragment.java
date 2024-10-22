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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

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
        Log.d("MyTag", "hoge");

        //Jsoupの処理を別スレッドで実行
        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int raceSuu = 1; raceSuu < 13; raceSuu++) {
                        // Webページを取得
                        Document doc;
                        if (raceSuu < 10) {
                            doc = Jsoup.connect("https://www.keibalab.jp/db/race/20241020050" + raceSuu + "/").get();
                        } else {
                            doc = Jsoup.connect("https://www.keibalab.jp/db/race/2024102005" + raceSuu + "/").get();
                        }
                        // クラス名が"content"の要素を抽出
                        Elements contentElements = doc.getElementsByClass("DbTable stripe resulttable");
                        contentElements = doc.select("tbody");
                        contentElements = doc.select("td");
                        Log.d("Mytag",contentElements.toString());

                        int j = 0;
                        for (int i = 0; i < 5; i++) {

                            // DB処理　データをインサート
                            dbManager.raceResultInsertData("20241020", "東京", String.valueOf(raceSuu), contentElements.get(j).text(),
                                    contentElements.get(j + 1).text(), contentElements.get(j + 2).text(), contentElements.get(j + 3).text(),
                                    contentElements.get(j + 4).text(), contentElements.get(j + 5).text(), contentElements.get(j + 6).text(),
                                    contentElements.get(j + 7).text(), contentElements.get(j + 8).text(), contentElements.get(j + 9).text(),
                                    contentElements.get(j + 10).text(), contentElements.get(j + 11).text(), contentElements.get(j + 12).text(),
                                    contentElements.get(j + 13).text(), contentElements.get(j + 14).text());
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
        }).start();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}