package com.tktkcompany.kakoRaceKeiba.ui.notifications;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentNotificationsBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentRaceresultsBinding;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationsFragment extends Fragment {
    public static android.app.AlertDialog progressDialog;
    private FragmentNotificationsBinding binding;
    private static AdView bannerAdView;
    private boolean isHeader = true;
    private TableLayout tableLayout1;
    private TableLayout tableLayout2;
    private TableLayout tableLayout3;
    private TableLayout tableLayout4;
    private TableLayout tableLayout5;
    private TableLayout tableLayout6;
    private TableLayout tableLayout7;
    private TableLayout tableLayout8;
    private TableLayout tableLayout9;
    private TableLayout tableLayout10;
    private TableLayout tableLayout11;
    private TableLayout tableLayout12;
    private TableLayout tableLayout13;
    private TableLayout tableLayout14;
    private View dialogView;

    private TextView raceText1;
    private TextView dateText1;
    private TextView dateText2;
    private TextView dateText3;
    private TextView dateText4;
    private TextView dateText5;
    private TextView dateText6;
    private TextView dateText7;
    private TextView dateText8;
    private TextView dateText9;
    private TextView dateText10;
    private TextView dateText11;
    private TextView dateText12;
    private TextView dateText13;
    private TextView dateText14;
    private TextView dateText15;
    private String joName = "中山";


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
        List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();
        dateList.add("20250106");
        // ユーザーが選択したアイテムを取得


        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        // ダイアログ用のレイアウトをインフレート
        dialogView = inflater.inflate(R.layout.dialog_layout, null, false);

        View root = binding.getRoot();

        TabLayout tabLayout = binding.tabLayout;
        ViewPager2 viewPager = binding.viewPager;

        // ページを設定するアダプター
        List<String> tabTitles = new ArrayList<>();
        tabTitles.add("好走馬");
        tabTitles.add("脚質");
        tabTitles.add("騎手");
        tabTitles.add("調教師");
        tabTitles.add("種牡馬");
        tabTitles.add("母父");
        tabTitles.add("払戻金");
        tabTitles.add("馬主");
        tabTitles.add("生産者");

        TabPagerAdapter adapter = new TabPagerAdapter(requireActivity(), tabTitles);
        viewPager.setAdapter(adapter);



        // AdViewのインスタンスを取得、ロード
        loadBannerAd();
        tableLayout1 = binding.tableLayout1;
        tableLayout2 = binding.tableLayout2;
        tableLayout3 = binding.tableLayout3;
        tableLayout4 = binding.tableLayout4;
        tableLayout5 = binding.tableLayout5;
        tableLayout6 = binding.tableLayout6;
        tableLayout7 = binding.tableLayout7;
        tableLayout8 = binding.tableLayout8;
        tableLayout9 = binding.tableLayout9;
        tableLayout10 = binding.tableLayout10;
        tableLayout11 = binding.tableLayout11;
        tableLayout12 = binding.tableLayout12;
        tableLayout13 = binding.tableLayout13;
        tableLayout14 = binding.tableLayout14;
        dateText1 = binding.textDate1;
        dateText2 = binding.textDate2;
        dateText3 = binding.textDate3;
        dateText4 = binding.textDate4;
        dateText5 = binding.textDate5;
        dateText6 = binding.textDate6;
        dateText7 = binding.textDate7;
        dateText8 = binding.textDate8;
        dateText9 = binding.textDate9;
        dateText10 = binding.textDate10;
        dateText11 = binding.textDate11;
        dateText12 = binding.textDate12;
        dateText13 = binding.textDate13;
        dateText14 = binding.textDate14;
        dateText15 = binding.textDate15;

        MyDatabaseManager dbManager = new MyDatabaseManager(getContext());
        dbManager.open();
        // ボタンを取得
        Button showDialogButton = binding.showDialogButton;
        // ボタンのクリックリスナーを設定
        showDialogButton.setOnClickListener(view -> showDialogList());

        // ダイアログを作成
        android.app.AlertDialog.Builder builder2 = new android.app.AlertDialog.Builder(getContext());
        builder2.setView(dialogView);
        builder2.setCancelable(false); // ダイアログ外をタップしても閉じないようにする
        progressDialog = builder2.create();
        // モーダルダイアログを表示
        showProgressDialog("読み込み中...");

        tableLayout1.removeAllViews();
        tableLayout2.removeAllViews();
        tableLayout3.removeAllViews();
        tableLayout4.removeAllViews();
        tableLayout5.removeAllViews();
        tableLayout6.removeAllViews();
        tableLayout7.removeAllViews();
        tableLayout8.removeAllViews();
        tableLayout9.removeAllViews();
        tableLayout10.removeAllViews();
        tableLayout11.removeAllViews();
        tableLayout12.removeAllViews();
        tableLayout13.removeAllViews();
        tableLayout14.removeAllViews();
        dateText1.setText("");
        dateText2.setText("");
        dateText3.setText("");
        dateText4.setText("");
        dateText5.setText("");
        dateText6.setText("");
        dateText7.setText("");
        dateText8.setText("");
        dateText9.setText("");
        dateText10.setText("");
        dateText11.setText("");
        dateText12.setText("");
        dateText13.setText("");
        dateText14.setText("");


        HashMap<String, TableLayout> tableMap = new HashMap<>();
        HashMap<String, TextView> textMap = new HashMap<>();
        int i = 0;
        for (String date : dateList) {
            if (i == 0) {
                tableMap.put(date, tableLayout1);
                textMap.put(date, dateText1);
            } else if (i == 1) {
                tableMap.put(date, tableLayout2);
                textMap.put(date, dateText2);
            } else if (i == 2) {
                tableMap.put(date, tableLayout3);
                textMap.put(date, dateText3);
            } else if (i == 3) {
                tableMap.put(date, tableLayout4);
                textMap.put(date, dateText4);
            } else if (i == 4) {
                tableMap.put(date, tableLayout5);
                textMap.put(date, dateText5);
            } else if (i == 5) {
                tableMap.put(date, tableLayout6);
                textMap.put(date, dateText6);
            } else if (i == 6) {
                tableMap.put(date, tableLayout7);
                textMap.put(date, dateText7);
            } else if (i == 7) {
                tableMap.put(date, tableLayout8);
                textMap.put(date, dateText8);
            } else if (i == 8) {
                tableMap.put(date, tableLayout9);
                textMap.put(date, dateText9);
            } else if (i == 9) {
                tableMap.put(date, tableLayout10);
                textMap.put(date, dateText10);
            } else if (i == 10) {
                tableMap.put(date, tableLayout11);
                textMap.put(date, dateText11);
            } else if (i == 11) {
                tableMap.put(date, tableLayout12);
                textMap.put(date, dateText12);
            } else if (i == 12) {
                tableMap.put(date, tableLayout13);
                textMap.put(date, dateText13);
            } else if (i == 13) {
                tableMap.put(date, tableLayout14);
                textMap.put(date, dateText14);
            }
            i = i + 1;
        }

        for (String date : dateList) {
            raceTrendsKousouHorseTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
            raceTrendsKousouHorseTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
        }

        // TabLayoutとViewPager2をリンク
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles.get(position))).attach();
        // タブ選択リスナーを追加
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //テーブル初期化
                tableLayout1.removeAllViews();
                tableLayout2.removeAllViews();
                tableLayout3.removeAllViews();
                tableLayout4.removeAllViews();
                tableLayout5.removeAllViews();
                tableLayout6.removeAllViews();
                tableLayout7.removeAllViews();
                tableLayout8.removeAllViews();
                tableLayout9.removeAllViews();
                tableLayout10.removeAllViews();
                tableLayout11.removeAllViews();
                tableLayout12.removeAllViews();
                tableLayout13.removeAllViews();
                tableLayout14.removeAllViews();
                dateText1.setText("");
                dateText2.setText("");
                dateText3.setText("");
                dateText4.setText("");
                dateText5.setText("");
                dateText6.setText("");
                dateText7.setText("");
                dateText8.setText("");
                dateText9.setText("");
                dateText10.setText("");
                dateText11.setText("");
                dateText12.setText("");
                dateText13.setText("");
                dateText14.setText("");
                // タブが選択されたときの処理
                int position = tab.getPosition(); // 選択されたタブのインデックス
                String tabTitle = tabTitles.get(position); // 選択されたタブのタイトル

                Toast.makeText(getContext(), "選択されたタブ: " + tabTitle, Toast.LENGTH_SHORT).show();
                // 必要に応じて、ここでボタン押下のイベント処理を追加
                // タブごとの処理を switch 文で分岐
                switch (position) {
                    case 0: // "好走馬" タブ
                        Toast.makeText(getContext(), "好走馬タブが選択されました", Toast.LENGTH_SHORT).show();
                        for (String date : dateList) {
                            raceTrendsKousouHorseTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKousouHorseTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 1: // "脚質" タブ
                        Toast.makeText(getContext(), "脚質タブが選択されました", Toast.LENGTH_SHORT).show();
                        // 必要な処理をここに記述
                        for (String date : dateList) {
                            raceTrendsKyakusituTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsKyakusituTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 2: // "騎手" タブ
                        Toast.makeText(getContext(), "騎手タブが選択されました", Toast.LENGTH_SHORT).show();
                        // 必要な処理をここに記述
                        for (String date : dateList) {
                            raceTrendsJockeyTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsJockeyTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 3: // "調教師" タブ
                        Toast.makeText(getContext(), "調教師タブが選択されました", Toast.LENGTH_SHORT).show();
                        for (String date : dateList) {
                            raceTrendsTyokyosiTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsTyokyosiTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 4: // "種牡馬" タブ
                        Toast.makeText(getContext(), "種牡馬タブが選択されました", Toast.LENGTH_SHORT).show();
                        for (String date : dateList) {
                            raceTrendsFartherTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsFartherTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 5: // "母父" タブ
                        Toast.makeText(getContext(), "母父タブが選択されました", Toast.LENGTH_SHORT).show();
                        for (String date : dateList) {
                            raceTrendsMatherTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsMatherTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 6: // "払戻金" タブ
                        Toast.makeText(getContext(), "払戻金タブが選択されました", Toast.LENGTH_SHORT).show();
                        for (String date : dateList) {
                            raceTrendsHaraimodosiTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsHaraimodosiTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 7: // "馬主" タブ
                        Toast.makeText(getContext(), "馬主タブが選択されました", Toast.LENGTH_SHORT).show();
                        for (String date : dateList) {
                            raceTrendsBanusiTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsBanusiTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    case 8: // "生産者" タブ
                        Toast.makeText(getContext(), "生産者タブが選択されました", Toast.LENGTH_SHORT).show();
                        for (String date : dateList) {
                            raceTrendsSeisanTableSet(date, "1R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "2R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "3R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "4R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "5R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "6R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "7R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "8R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "9R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "10R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "11R", joName, tableMap.get(date), textMap.get(date));
                            raceTrendsSeisanTableSet(date, "12R", joName, tableMap.get(date), textMap.get(date));
                        }
                        break;

                    default:
                        Toast.makeText(getContext(), "未知のタブが選択されました", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // タブが選択解除されたときの処理（必要なら実装）
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // すでに選択されているタブが再度タップされたときの処理（必要なら実装）
            }
        });


        hideProgressDialog();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void raceTrendsKousouHorseTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String umaban1Name = childSnapshot.child("uma1Name").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String umaban2Name = childSnapshot.child("uma2Name").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String umaban3Name = childSnapshot.child("umaName").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(umaban1Name));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(umaban2Name));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(umaban3Name));
                            tableLayout.addView(tableRow);
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }


    private void raceTrendsKyakusituTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String uma1Kyakusitu = childSnapshot.child("uma1Kyakusitu").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String uma2Kyakusitu = childSnapshot.child("uma2Kyakusitu").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String uma3Kyakusitu = childSnapshot.child("uma3Kyakusitu").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(uma1Kyakusitu));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(uma2Kyakusitu));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(uma3Kyakusitu));
                            tableLayout.addView(tableRow);
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }

    private void raceTrendsJockeyTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String uma1Jockey = childSnapshot.child("uma1Jockey").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String uma2Jockey = childSnapshot.child("uma2Jockey").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String uma3Jockey = childSnapshot.child("uma3Jockey").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(uma1Jockey));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(uma2Jockey));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(uma3Jockey));
                            tableLayout.addView(tableRow);
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }

    private void raceTrendsTyokyosiTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String uma1tyokyosi = childSnapshot.child("uma1tyokyosi").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String uma2tyokyosi = childSnapshot.child("uma2tyokyosi").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String uma3tyokyosi = childSnapshot.child("uma3tyokyosi").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(uma1tyokyosi));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(uma2tyokyosi));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(uma3tyokyosi));
                            tableLayout.addView(tableRow);
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }

    private void raceTrendsFartherTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String uma1father = childSnapshot.child("uma1father").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String uma2father = childSnapshot.child("uma2father").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String uma3father = childSnapshot.child("uma3father").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(uma1father));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(uma2father));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(uma3father));
                            tableLayout.addView(tableRow);
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }

    private void raceTrendsMatherTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String uma1mather = childSnapshot.child("uma1mather").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String uma2mather = childSnapshot.child("uma2mather").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String uma3mather = childSnapshot.child("uma3mather").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(uma1mather));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(uma2mather));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(uma3mather));
                            tableLayout.addView(tableRow);
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }

    private void raceTrendsHaraimodosiTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String haraimodosiAre = childSnapshot.child("haraimodosiAre").getValue(String.class);
                        String haraimodosiTansyo = childSnapshot.child("haraimodosiTansyo").getValue(String.class);
                        String haraimodosiUmaren = childSnapshot.child("haraimodosiUmaren").getValue(String.class);
                        String haraimodosi3Rentan = childSnapshot.child("haraimodosi3Rentan").getValue(String.class);

                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("荒れ"));
                                tableRowRetu.addView(createTextView("単勝"));
                                tableRowRetu.addView(createTextView("馬連"));
                                tableRowRetu.addView(createTextView("３連単"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(haraimodosiAre));
                            //単勝
                            tableRow.addView(createTextView(haraimodosiTansyo));
                            //馬連
                            tableRow.addView(createTextView(haraimodosiUmaren));
                            //3連単
                            tableRow.addView(createTextView(haraimodosi3Rentan));
                            tableLayout.addView(tableRow);
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }

    private void raceTrendsBanusiTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String banusi1 = childSnapshot.child("banusi1").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String banusi2 = childSnapshot.child("banusi2").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String banusi3 = childSnapshot.child("banusi3").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(banusi1));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(banusi2));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(banusi3));
                            tableLayout.addView(tableRow);
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
            }
        });
    }


    private void raceTrendsSeisanTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    // Rで開催場所が一致している場合にのみ処理
                    if (raceNo.equals(sRaceNo) & kaisaijo.equals(sKaisaijo) && sKaisaibi.equals(kaisaibi)) {
                        if (sRaceNo.equals("1R")) {
                            isHeader = true;
                        }
                        String sRaceNumber = childSnapshot.child("raceNum").getValue(String.class);
                        String sRaceName = childSnapshot.child("raceName").getValue(String.class);
                        String sZyouken = childSnapshot.child("raceDistance").getValue(String.class);
                        String sbabaCondition = childSnapshot.child("raceCondition").getValue(String.class);
                        String umaban1 = childSnapshot.child("uma1Ban").getValue(String.class);
                        String seisann1 = childSnapshot.child("seisann1").getValue(String.class);
                        String umaban2 = childSnapshot.child("uma2Ban").getValue(String.class);
                        String seisann2 = childSnapshot.child("seisann2").getValue(String.class);
                        String umaban3 = childSnapshot.child("uma3Ban").getValue(String.class);
                        String seisann3 = childSnapshot.child("seisann3").getValue(String.class);


                        //レースタイトルセット
                        dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                        dateTextLayout.setPadding(16, 8, 16, 8);
                        requireActivity().runOnUiThread(() -> {
                            if (isHeader) {
                                // table見出し設定
                                TableRow tableRowRetu = new TableRow(getActivity());
                                tableRowRetu.addView(createTextView("R"));
                                tableRowRetu.addView(createTextView("レース名"));
                                tableRowRetu.addView(createTextView("条件"));
                                tableRowRetu.addView(createTextView("馬場・天候"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("１着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("２着(人気)"));
                                tableRowRetu.addView(createTextView("馬番"));
                                tableRowRetu.addView(createTextView("３着(人気)"));
                                tableRowRetu.setBackgroundColor(Color.LTGRAY);
                                tableLayout.addView(tableRowRetu);
                                isHeader = false;
                            }

                            TableRow tableRow = new TableRow(getActivity());
                            //R
                            tableRow.addView(createTextView(sRaceNumber));
                            // レース名
                            tableRow.addView(createTextView(sRaceName));
                            // 条件
                            tableRow.addView(createTextView(sZyouken));
                            // 馬場・天候
                            tableRow.addView(createTextView(sbabaCondition));
                            //馬番1
                            tableRow.addView(createTextView(umaban1));
                            //１着(人気)
                            tableRow.addView(createTextView(seisann1));
                            //馬番2
                            tableRow.addView(createTextView(umaban2));
                            //2着(人気)
                            tableRow.addView(createTextView(seisann2));
                            //馬番3
                            tableRow.addView(createTextView(umaban3));
                            //3着(人気)
                            tableRow.addView(createTextView(seisann3));
                            tableLayout.addView(tableRow);
                        });
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


    private void showDialogList() {
        // ダイアログに表示するリスト
        String[] items = {"東京", "中山", "京都", "中京"};

        // AlertDialog を作成
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose an Option")
                .setItems(items, (dialog, which) -> {
                    // ユーザーが選択したアイテムを取得
                    joName = items[which];
                });

        // ダイアログを表示
        builder.create().show();
    }


    // モーダルダイアログを表示するメソッド
    private void showProgressDialog(String message) {
        if (progressDialog != null && !progressDialog.isShowing()) {
//            progressDialog.show(); // ダイアログを表示
        }
    }

    // モーダルダイアログを非表示にするメソッド
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss(); // ダイアログを閉じる
        }
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
            public void onAdFailedToLoad(LoadAdError adError) {
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