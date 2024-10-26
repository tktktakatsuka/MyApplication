package com.example.myapplication.ui.raceResult;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.databinding.FragmentRaceresultsBinding;
import com.example.myapplication.db.MyDatabaseHelper;
import com.example.myapplication.db.MyDatabaseManager;

import java.util.List;

public class RaceResultsFragment extends Fragment {
    private MyDatabaseHelper dbHelper;

    private FragmentRaceresultsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRaceresultsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MyDatabaseManager dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

        // DatabaseHelperの初期化
        dbHelper = new MyDatabaseHelper(getContext());
        // TableLayoutをレイアウトから取得
        final TableLayout tableLayout2 = binding.tableLayout2;
        final TableLayout tableLayout1 = binding.tableLayout1;
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

        final TextView raceText1 = binding.textDashboard1;
        final TextView raceText2 = binding.textDashboard2;
        final TextView raceText3 = binding.textDashboard3;
        final TextView raceText4 = binding.textDashboard4;
        final TextView raceText5 = binding.textDashboard5;
        final TextView raceText6 = binding.textDashboard6;
        final TextView raceText7 = binding.textDashboard7;
        final TextView raceText8 = binding.textDashboard8;
        final TextView raceText9 = binding.textDashboard9;
        final TextView raceText10 = binding.textDashboard10;
        final TextView raceText11 = binding.textDashboard11;
        final TextView raceText12 = binding.textDashboard12;

        final TextView hassouText1 = binding.textHassouTime1;
        final TextView hassouText2 = binding.textHassouTime2;
        final TextView hassouText3 = binding.textHassouTime3;
        final TextView hassouText4 = binding.textHassouTime4;
        final TextView hassouText5 = binding.textHassouTime5;
        final TextView hassouText6 = binding.textHassouTime6;
        final TextView hassouText7 = binding.textHassouTime7;
        final TextView hassouText8 = binding.textHassouTime8;
        final TextView hassouText9 = binding.textHassouTime9;
        final TextView hassouText10 = binding.textHassouTime10;
        final TextView hassouText11 = binding.textHassouTime11;
        final TextView hassouText12 = binding.textHassouTime12;


        // 渡された値を取得する
        Bundle args = getArguments();
        String receivedValue = "";
        String receivedJoValue = "";
        if (args != null) {
            // ここで "key" に対応する値を取得
            receivedValue = args.getString("key");
            receivedJoValue = args.getString("jo");

        }

        raceResultTableSet(receivedValue, "1", receivedJoValue, dbManager, tableLayout1,raceText1,hassouText1);
        raceResultTableSet(receivedValue, "2", receivedJoValue, dbManager, tableLayout2,raceText2,hassouText2);
        raceResultTableSet(receivedValue, "3", receivedJoValue, dbManager, tableLayout3,raceText3,hassouText3);
        raceResultTableSet(receivedValue, "4", receivedJoValue, dbManager, tableLayout4,raceText4,hassouText4);
        raceResultTableSet(receivedValue, "5", receivedJoValue, dbManager, tableLayout5,raceText5, hassouText5);
        raceResultTableSet(receivedValue, "6", receivedJoValue, dbManager, tableLayout6,raceText6, hassouText6);
        raceResultTableSet(receivedValue, "7", receivedJoValue, dbManager, tableLayout7,raceText7, hassouText7);
        raceResultTableSet(receivedValue, "8", receivedJoValue, dbManager, tableLayout8,raceText8, hassouText8);
        raceResultTableSet(receivedValue, "9", receivedJoValue, dbManager, tableLayout9,raceText9, hassouText9);
        raceResultTableSet(receivedValue, "10", receivedJoValue, dbManager, tableLayout10,raceText10, hassouText10);
        raceResultTableSet(receivedValue, "11", receivedJoValue, dbManager, tableLayout11,raceText11, hassouText11);
        raceResultTableSet(receivedValue, "12", receivedJoValue, dbManager, tableLayout12,raceText12, hassouText12);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void raceResultTableSet(String kaisaibi, String raceNo, String kaisaijo, MyDatabaseManager dbManager, TableLayout tableLayout, TextView raceTitle, TextView hassouTime) {

        List<String> list = dbManager.getRaceResults(kaisaibi, raceNo, kaisaijo);
        int recordTani = 0;

        raceTitle.setText(raceNo+"R"+"   "+list.get(8));
        raceTitle.setPadding(16, 8, 16, 8);


        String text = list.get(9);
        String targetWord = "馬齢";
        // targetWordの位置を探す
        int index = text.indexOf(targetWord);
        String result="";

        if (index != -1) {
            // targetWordまでの文字を削除
            result = text.substring(index);
        }

        String targetWord2 = "発走";
        // targetWordの位置を探す
        int index2 = text.indexOf(targetWord2);

        if (index2 != -1) {
            // targetWordの直前までの文字列を取得
            result = text.substring(0, index2 + targetWord2.length());
        }

        hassouTime.setText(result);
        hassouTime.setTextSize(12);
        hassouTime.setPadding(16, 8, 16, 16);

        list.removeIf(item -> item.equals(list.get(8)));
        list.removeIf(item -> item.equals(list.get(8)));

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
            textView8.setText(list.get(recordTani + 7));
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