package com.example.myapplication.ui.dashboard;


import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.example.myapplication.db.MyDatabaseHelper;
import com.example.myapplication.db.MyDatabaseManager;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.List;

public class DashboardFragment extends Fragment {
    private MyDatabaseHelper dbHelper;

    private FragmentDashboardBinding binding;

    /**
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
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

        // DatabaseHelperの初期化
        dbHelper = new MyDatabaseHelper(getContext());
        // TableLayoutをレイアウトから取得
        final TableLayout tableLayout1 = binding.tableLayout1;
        final TableLayout tableLayout2 = binding.tableLayout2;
        final TableLayout tableLayout3 = binding.tableLayout3;
        final TableLayout tableLayout4 = binding.tableLayout4;
        final TableLayout tableLayout5 = binding.tableLayout5;
        final TableLayout tableLayout6 = binding.tableLayout6;
        final TableLayout tableLayout7 = binding.tableLayout7;
        final TableLayout tableLayout8 = binding.tableLayout8;
        final TableLayout tableLayout9 = binding.tableLayout9;
        final TableLayout tableLayout10 = binding.tableLayout10;
        final TableLayout tableLayout11 = binding.tableLayout11;
        final TableLayout tableLayout12 = binding.tableLayout12;

        raceResultTableSet("20241020", "1", "東京", dbManager, tableLayout1);
        raceResultTableSet("20241020", "2", "東京", dbManager, tableLayout2);
        raceResultTableSet("20241020", "3", "東京", dbManager, tableLayout3);
        raceResultTableSet("20241020", "4", "東京", dbManager, tableLayout4);
        raceResultTableSet("20241020", "5", "東京", dbManager, tableLayout5);
        raceResultTableSet("20241020", "6", "東京", dbManager, tableLayout6);
        raceResultTableSet("20241020", "7", "東京", dbManager, tableLayout7);
        raceResultTableSet("20241020", "8", "東京", dbManager, tableLayout8);
        raceResultTableSet("20241020", "9", "東京", dbManager, tableLayout9);
        raceResultTableSet("20241020", "10", "東京", dbManager, tableLayout10);
        raceResultTableSet("20241020", "11", "東京", dbManager, tableLayout11);
        raceResultTableSet("20241020", "12", "東京", dbManager, tableLayout12);

        // ボタンをクリックしたらFragmentCへ遷移する
        Button button = root.findViewById(R.id.button_sample);
        button.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_fragmentB_to_fragmentC);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void raceResultTableSet(String kaisaibi, String raceNo, String kaisaijo, MyDatabaseManager dbManager, TableLayout tableLayout) {

        List<String> list = dbManager.getRaceResults(kaisaibi, raceNo, kaisaijo);
        int recordTani = 0;

        // table見出し設定
        TableRow tableRowRetu = new TableRow(getActivity());
        TextView textViewRetu1 = new TextView(getActivity());
        textViewRetu1.setText("着");
        textViewRetu1.setPadding(16, 8, 16, 8);

        TextView textViewRetu2 = new TextView(getActivity());
        textViewRetu2.setText("枠");
        textViewRetu2.setPadding(16, 8, 16, 8);

        TextView textViewRetu3 = new TextView(getActivity());
        textViewRetu3.setText("馬名");
        textViewRetu3.setPadding(16, 8, 16, 8);

        TextView textViewRetu4 = new TextView(getActivity());
        textViewRetu4.setText("性齢");
        textViewRetu4.setPadding(16, 8, 16, 8);

        TextView textViewRetu5 = new TextView(getActivity());
        textViewRetu5.setText("騎手");
        textViewRetu5.setPadding(16, 8, 16, 8);

        TextView textViewRetu6 = new TextView(getActivity());
        textViewRetu6.setText("人気");
        textViewRetu6.setPadding(16, 8, 16, 8);

        TextView textViewRetu7 = new TextView(getActivity());
        textViewRetu7.setText("単勝");
        textViewRetu7.setPadding(16, 8, 16, 8);

        TextView textViewRetu8 = new TextView(getActivity());
        textViewRetu8.setText("タイム");
        textViewRetu8.setPadding(16, 8, 16, 8);

        // Add TextViews to TableRow
        tableRowRetu.addView(textViewRetu1);
        tableRowRetu.addView(textViewRetu2);
        tableRowRetu.addView(textViewRetu3);
        tableRowRetu.addView(textViewRetu4);
        tableRowRetu.addView(textViewRetu5);
        tableRowRetu.addView(textViewRetu6);
        tableRowRetu.addView(textViewRetu7);
        tableRowRetu.addView(textViewRetu8);


        // 列設定
        tableRowRetu.setBackgroundColor(Color.LTGRAY);
        tableLayout.addView(tableRowRetu);

        // Add ro4ws dynamically
        for (int i = 1; i <= 5; i++) {
            TableRow tableRow = new TableRow(getActivity());

            TextView textView1 = new TextView(getActivity());
            textView1.setText(list.get(recordTani));
            textView1.setPadding(16, 8, 16, 8);

            TextView textView2 = new TextView(getActivity());
            textView2.setText(list.get(recordTani + 1));
            textView2.setPadding(16, 8, 16, 8);

            TextView textView3 = new TextView(getActivity());
            textView3.setText(list.get(recordTani + 2));
            textView3.setPadding(16, 8, 16, 8);

            TextView textView4 = new TextView(getActivity());
            textView4.setText(list.get(recordTani + 3));
            textView4.setPadding(16, 8, 16, 8);

            TextView textView5 = new TextView(getActivity());
            textView5.setText(list.get(recordTani + 4));
            textView5.setPadding(16, 8, 16, 8);

            TextView textView6 = new TextView(getActivity());
            textView6.setText(list.get(recordTani + 5));
            textView6.setPadding(16, 8, 16, 8);

            TextView textView7 = new TextView(getActivity());
            textView7.setText(list.get(recordTani + 6));
            textView7.setPadding(16, 8, 16, 8);

            TextView textView8 = new TextView(getActivity());
            textView8.setText(list.get(recordTani + 6));
            textView8.setPadding(16, 8, 16, 8);

            // Add TextViews to TableRow
            tableRow.addView(textView1);
            tableRow.addView(textView2);
            tableRow.addView(textView3);
            tableRow.addView(textView4);
            tableRow.addView(textView5);
            tableRow.addView(textView6);
            tableRow.addView(textView7);
            tableRow.addView(textView8);

            // Add TableRow to TableLayout
            tableLayout.addView(tableRow);
            recordTani = recordTani + 8;
        }
    }

}