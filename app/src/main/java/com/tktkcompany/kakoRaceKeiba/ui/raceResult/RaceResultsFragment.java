package com.tktkcompany.kakoRaceKeiba.ui.raceResult;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentRaceresultsBinding;

public class RaceResultsFragment extends Fragment {
    private static AdView bannerAdView;
    private FragmentRaceresultsBinding binding;

    /**
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRaceresultsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // AdViewのインスタンスを取得、ロード
        loadBannerAd();
        MyDatabaseManager dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

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
            // "key" に対応する値を取得
            receivedValue = args.getString("key");
            receivedJoValue = args.getString("jo");

        }

        raceResultTableSet(receivedValue, "1", receivedJoValue, "1", tableLayout1, raceText1, hassouText1);
        raceResultTableSet(receivedValue, "2", receivedJoValue, "1", tableLayout2, raceText2, hassouText2);
        raceResultTableSet(receivedValue, "3", receivedJoValue, "1", tableLayout3, raceText3, hassouText3);
        raceResultTableSet(receivedValue, "4", receivedJoValue, "1", tableLayout4, raceText4, hassouText4);
        raceResultTableSet(receivedValue, "5", receivedJoValue, "1", tableLayout5, raceText5, hassouText5);
        raceResultTableSet(receivedValue, "6", receivedJoValue, "1", tableLayout6, raceText6, hassouText6);
        raceResultTableSet(receivedValue, "7", receivedJoValue, "1", tableLayout7, raceText7, hassouText7);
        raceResultTableSet(receivedValue, "8", receivedJoValue, "1", tableLayout8, raceText8, hassouText8);
        raceResultTableSet(receivedValue, "9", receivedJoValue, "1", tableLayout9, raceText9, hassouText9);
        raceResultTableSet(receivedValue, "10", receivedJoValue, "1", tableLayout10, raceText10, hassouText10);
        raceResultTableSet(receivedValue, "11", receivedJoValue, "1", tableLayout11, raceText11, hassouText11);
        raceResultTableSet(receivedValue, "12", receivedJoValue, "1", tableLayout12, raceText12, hassouText12);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void raceResultTableSet(String kaisaibi, String raceNo, String kaisaijo, String tyaku, TableLayout tableLayout, TextView raceTitle, TextView hassouTime) {

        FirebaseManager.queryData("raceResult" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                boolean isHeader = true;
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {

                    String sRaceNo = childSnapshot.child("raceNo").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);
                    // 1Rで開催場所が一致している場合にのみ処理

                    if (raceNo.equals(sRaceNo) && kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        String sRaceTitle = childSnapshot.child("raceTitle").getValue(String.class);
                        String sWaku = childSnapshot.child("waku").getValue(String.class);
                        String sAge = childSnapshot.child("age").getValue(String.class);
                        String sHorseName = childSnapshot.child("horseName").getValue(String.class);
                        String sJockey = childSnapshot.child("jockey").getValue(String.class);
                        String sPopular = childSnapshot.child("popular").getValue(String.class);
                        String sWinOdds = childSnapshot.child("winOdds").getValue(String.class);
                        String sTime = childSnapshot.child("time").getValue(String.class);
                        String sTyaku = childSnapshot.child("tyaku").getValue(String.class);

                        //レースタイトルセット
                        raceTitle.setText(raceNo + "R" + "   " + sRaceTitle);
                        raceTitle.setPadding(16, 8, 16, 8);

                        //発走時刻セット
                        String sHassouTime = childSnapshot.child("hassouTime").getValue(String.class);
                        String targetWord = "馬齢";
                        // targetWordの位置を探す
                        assert sHassouTime != null;
                        int index = sHassouTime.indexOf(targetWord);
                        String result = "";
                        if (index != -1) {
                            // targetWordまでの文字を削除
                            result = sHassouTime.substring(index);
                        }
                        String targetWord2 = "発走";
                        // targetWordの位置を探す
                        int index2 = sHassouTime.indexOf(targetWord2);
                        if (index2 != -1) {
                            // targetWordの直前までの文字列を取得
                            result = sHassouTime.substring(0, index2 + targetWord2.length());
                        }
                        hassouTime.setText(result);
                        hassouTime.setTextSize(12);
                        hassouTime.setPadding(16, 8, 16, 16);


                        if (isHeader) {
                            // table見出し設定
                            TableRow tableRowRetu = new TableRow(getActivity());
                            tableRowRetu.addView(createTextView("着"));
                            tableRowRetu.addView(createTextView("枠"));
                            tableRowRetu.addView(createTextView("馬名"));
                            tableRowRetu.addView(createTextView("性齢"));
                            tableRowRetu.addView(createTextView("騎手"));
                            tableRowRetu.addView(createTextView("人気"));
                            tableRowRetu.addView(createTextView("単勝"));
                            tableRowRetu.addView(createTextView("タイム"));
                            tableRowRetu.setBackgroundColor(Color.LTGRAY);
                            tableLayout.addView(tableRowRetu);
                        }


                        TableRow tableRow = new TableRow(getActivity());
                        // Add TextViews to TableRow
                        //着
                        tableRow.addView(createTextView(sTyaku));
                        // 枠
                        tableRow.addView(createTextView(sWaku));
                        // 馬名
                        tableRow.addView(createTextView(sHorseName));
                        // 性齢
                        tableRow.addView(createTextView(sAge));
                        //騎手
                        tableRow.addView(createTextView(sJockey));
                        //人気
                        tableRow.addView(createTextView(sPopular));
                        //単勝
                        tableRow.addView(createTextView(sWinOdds));
                        //タイム
                        tableRow.addView(createTextView(sTime));
                        tableLayout.addView(tableRow);
                        isHeader = false;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }

    // テキストビューを動的に生成するヘルパーメソッド
    private TextView createTextView(String text) {
        GradientDrawable border = new GradientDrawable();
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        border.setStroke(2, Color.BLACK); // 黒い線で幅2pxのボーダー
        textView.setBackground(border);
        return textView;
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
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
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