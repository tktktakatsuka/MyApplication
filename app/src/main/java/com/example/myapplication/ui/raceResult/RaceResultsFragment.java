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

import com.example.myapplication.databinding.FragmentNotificationsBinding;
import com.example.myapplication.databinding.FragmentRaceresultsBinding;
import com.example.myapplication.db.MyDatabaseHelper;
import com.example.myapplication.db.MyDatabaseManager;

import java.util.List;

public class RaceResultsFragment extends Fragment {
    private MyDatabaseHelper dbHelper;

    private FragmentRaceresultsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RaceResultsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(RaceResultsViewModel.class);

        binding = FragmentRaceresultsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MyDatabaseManager dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

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

        // 渡された値を取得する
        Bundle args = getArguments();
        String receivedValue = "";
        String receivedJoValue = "";
        if (args != null) {
            // ここで "key" に対応する値を取得
            receivedValue = args.getString("key");
            receivedJoValue = args.getString("jo");

        }

        raceResultTableSet(receivedValue, "1", receivedJoValue, dbManager, tableLayout1);
        raceResultTableSet(receivedValue, "2", receivedJoValue, dbManager, tableLayout2);
        raceResultTableSet(receivedValue, "3", receivedJoValue, dbManager, tableLayout3);
        raceResultTableSet(receivedValue, "4", receivedJoValue, dbManager, tableLayout4);
        raceResultTableSet(receivedValue, "5", receivedJoValue, dbManager, tableLayout5);
        raceResultTableSet(receivedValue, "6", receivedJoValue, dbManager, tableLayout6);
        raceResultTableSet(receivedValue, "7", receivedJoValue, dbManager, tableLayout7);
        raceResultTableSet(receivedValue, "8", receivedJoValue, dbManager, tableLayout8);
        raceResultTableSet(receivedValue, "9", receivedJoValue, dbManager, tableLayout9);
        raceResultTableSet(receivedValue, "10", receivedJoValue, dbManager, tableLayout10);
        raceResultTableSet(receivedValue, "11", receivedJoValue, dbManager, tableLayout11);
        raceResultTableSet(receivedValue, "12", receivedJoValue, dbManager, tableLayout12);

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