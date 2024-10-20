package com.example.myapplication.ui.dashboard;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Handler;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.example.myapplication.db.MyDatabaseManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


import java.io.IOException;

public class DashboardFragment extends Fragment {
    //全角カナ
    private static final String JUDG_STRING = "^[\\u30A0-\\u30FF]+$";

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //DB接続
        MyDatabaseManager dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

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
                        //log出力
                        Log.d("JsoupThread", "" + contentElements);

                        // 結果を一つの文字列にまとめる
                        StringBuilder result = new StringBuilder();


                        int j = 0;
                        for (int i = 0; i < 5; i++) {
                            // DB処理　データをインサート

                            dbManager.raceResultInsertData("20241020", String.valueOf(raceSuu), contentElements.get(j).text(),
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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 全角カナチェック
     * <p>
     * 指定した文字列が全て全角カタカナであるか否かをチェックします
     * ※nullはfalse判定されます
     *
     * @param str チェック対象の文字列
     * @return true：全て全角カタカナ  false：全角カタカナ以外の文字が含まれている
     */
    public static boolean isFullKana(String str) {
        // nullチェック
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 各文字がカタカナの範囲に含まれるかをチェック
        for (char c : str.toCharArray()) {
            if (!(c >= '\u30A0' && c <= '\u30FF')) {
                return false; // カタカナ以外の文字が含まれていたらfalseを返す
            }
        }
        // 全てカタカナの場合はtrue
        return true;
    }
}