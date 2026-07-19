package com.tktkcompany.kakoRaceKeiba.ui.notifications;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentNotificationsBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;

import java.util.Collections;
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
    private TableLayout tableLayout15;
    private TableLayout tableLayout16;
    private TableLayout tableLayout17;
    private TableLayout tableLayout18;
    private TableLayout tableLayout19;
    private TableLayout tableLayout20;

    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

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
    private TextView dateText16;
    private TextView dateText17;
    private TextView dateText18;
    private TextView dateText19;
    private TextView dateText20;
    private List<String> joNameList = new ArrayList<String>();



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

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        CheckBox checkBox = binding.checkboxExample;
        View root = binding.getRoot();

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getJoNames().observe(getViewLifecycleOwner(), joNames -> {
            joNameList = (joNames);

            // joNames（競馬場名リスト）をここで使える！
            binding.selectRaceText.setText(joNameList.get(0) + "競馬場の傾向を表示");
            List<String> dateList = WeekendDays.getPastWeekendsInCurrentMonth();
            if (checkBox.isChecked()) {
                Collections.sort(dateList, Collections.reverseOrder());
                checkBox.setChecked(true);
            } else {
                Collections.sort(dateList);
            }

            //スピナー表示
            progressBar = binding.progressBar;
            progressBar.setVisibility(View.VISIBLE);

            tabLayout = binding.tabLayout;
            viewPager = binding.viewPager;
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
            tableLayout15 = binding.tableLayout15;
            tableLayout16 = binding.tableLayout16;
            tableLayout17 = binding.tableLayout17;
            tableLayout18 = binding.tableLayout18;
            tableLayout19 = binding.tableLayout19;
            tableLayout20 = binding.tableLayout20;
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
            dateText16 = binding.textDate16;
            dateText17 = binding.textDate17;
            dateText18 = binding.textDate18;
            dateText19 = binding.textDate19;
            dateText20 = binding.textDate20;


            // ボタンを取得
            Button showDialogButton = binding.showDialogButton;
            // ボタンのクリックリスナーを設定
            showDialogButton.setOnClickListener(view -> showDialogList());

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
                } else if (i == 14) {
                    tableMap.put(date, tableLayout15);
                    textMap.put(date, dateText15);
                } else if (i == 15) {
                    tableMap.put(date, tableLayout16);
                    textMap.put(date, dateText16);
                } else if (i == 16) {
                    tableMap.put(date, tableLayout17);
                    textMap.put(date, dateText17);
                } else if (i == 17) {
                    tableMap.put(date, tableLayout18);
                    textMap.put(date, dateText18);
                } else if (i == 18) {
                    tableMap.put(date, tableLayout19);
                    textMap.put(date, dateText19);
                } else if (i == 19) {
                    tableMap.put(date, tableLayout20);
                    textMap.put(date, dateText20);
                }
                i = i + 1;
            }

            CheckBox checkboxHani = binding.checkboxHani;
            int reptNumber = 0;
            for (String date : dateList) {
                if (!checkboxHani.isChecked() && reptNumber == 8) {
                    break;
                } else {
                    raceTrendsKousouHorseTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    raceTrendsKousouHorseTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                    reptNumber = reptNumber + 1;
                }
            }


            // TabLayoutとViewPager2をリンク
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles.get(position))).attach();
            // タブ選択リスナーを追加
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    if (checkBox.isChecked()) {
                        Collections.sort(dateList, Collections.reverseOrder());
                    } else {
                        Collections.sort(dateList);
                    }
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
                        } else if (i == 14) {
                            tableMap.put(date, tableLayout15);
                            textMap.put(date, dateText15);
                        } else if (i == 15) {
                            tableMap.put(date, tableLayout16);
                            textMap.put(date, dateText16);
                        } else if (i == 16) {
                            tableMap.put(date, tableLayout17);
                            textMap.put(date, dateText17);
                        } else if (i == 17) {
                            tableMap.put(date, tableLayout18);
                            textMap.put(date, dateText18);
                        } else if (i == 18) {
                            tableMap.put(date, tableLayout19);
                            textMap.put(date, dateText19);
                        } else if (i == 19) {
                            tableMap.put(date, tableLayout20);
                            textMap.put(date, dateText20);
                        }
                        i = i + 1;
                    }

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
                    tableLayout15.removeAllViews();
                    tableLayout16.removeAllViews();
                    tableLayout17.removeAllViews();
                    tableLayout18.removeAllViews();
                    tableLayout19.removeAllViews();
                    tableLayout20.removeAllViews();
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
                    dateText15.setText("");
                    dateText16.setText("");
                    dateText17.setText("");
                    dateText18.setText("");
                    dateText19.setText("");
                    dateText20.setText("");
                    // タブが選択されたときの処理
                    int position = tab.getPosition(); // 選択されたタブのインデックス

                    CheckBox checkboxHani = binding.checkboxHani;
                    int reptNumber = 0;

                    switch (position) {
                        case 0: // "好走馬" タブ
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsKousouHorseTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKousouHorseTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }

                            }
                            break;

                        case 1: // "脚質" タブ
                            Toast.makeText(getContext(), "脚質タブが選択されました", Toast.LENGTH_SHORT).show();
                            // 必要な処理をここに記述
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsKyakusituTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsKyakusituTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
                            }
                            break;

                        case 2: // "騎手" タブ
                            Toast.makeText(getContext(), "騎手タブが選択されました", Toast.LENGTH_SHORT).show();
                            // 必要な処理をここに記述
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsJockeyTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsJockeyTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
                            }
                            break;

                        case 3: // "調教師" タブ

                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsTyokyosiTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsTyokyosiTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
                            }
                            break;

                        case 4: // "種牡馬" タブ
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsFartherTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsFartherTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
                            }
                            break;

                        case 5: // "母父" タブ
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsMatherTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsMatherTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
                            }
                            break;

                        case 6: // "払戻金" タブ
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsHaraimodosiTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsHaraimodosiTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
                            }
                            break;

                        case 7: // "馬主" タブ
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsBanusiTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsBanusiTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
                            }
                            break;

                        case 8: // "生産者" タブ
                            for (String date : dateList) {
                                if (!checkboxHani.isChecked() && reptNumber == 8) {
                                    break;
                                } else {
                                    raceTrendsSeisanTableSet(date, "1R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "2R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "3R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "4R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "5R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "6R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "7R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "8R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "9R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "10R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "11R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    raceTrendsSeisanTableSet(date, "12R", joNameList.get(0), tableMap.get(date), textMap.get(date));
                                    reptNumber = reptNumber + 1;
                                }
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
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void raceTrendsKousouHorseTableSet(String kaisaibi, String raceNo, String kaisaijo, TableLayout tableLayout, TextView dateTextLayout) {
        FirebaseManager.queryData("raceTrends" + "/" + kaisaijo, "kaisaibi", kaisaibi, new ValueEventListener() {

            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //スピナー表示
                if ("1R".equals(raceNo)) {
                    progressBar = binding.progressBar;
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.VISIBLE);
                    });
                }

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNum").getValue(String.class);
                    String sKaisaijo = childSnapshot.child("kaisaijo").getValue(String.class);
                    String sKaisaibi = childSnapshot.child("kaisaibi").getValue(String.class);

                    if (sRaceNo == null || sKaisaijo == null || sKaisaibi == null) continue;

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

                        if (dateTextLayout != null) {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
                        }

                        if (sZyouken == null) continue;

                        getActivity().runOnUiThread(() -> {
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
                                if (tableLayout != null) {
                                    tableLayout.addView(tableRowRetu);
                                }
                                isHeader = false;
                            }
                        });

                        if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {
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
                            if (tableLayout != null) {
                                tableLayout.addView(tableRow);
                            }
                        }

                        if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {
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
                            if (tableLayout != null) {
                                tableLayout.addView(tableRow);
                            }
                        }
                    }
                }

                //12R目の処理終了時に読み込みをオフにする
                if ("12R".equals(raceNo)) {
                    if (isAdded() && progressBar != null) {
                        progressBar.setVisibility(View.GONE);
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
            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;

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

                        if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {

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
                        }

                        if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {

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
                        }

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
            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;

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
                        requireActivity().runOnUiThread(() -> {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
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

                            if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {

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
                            }

                            if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {
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
                            }
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
            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;
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

                        requireActivity().runOnUiThread(() -> {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
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

                            if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {

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
                            }

                            if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {

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
                            }
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
            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;
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

                        requireActivity().runOnUiThread(() -> {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
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

                            if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {


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
                            }

                            if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {
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
                            }
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
            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;
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

                        requireActivity().runOnUiThread(() -> {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
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

                            if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {

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
                            }

                            if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {

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
                            }
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
            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;

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

                        requireActivity().runOnUiThread(() -> {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
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

                            if (checkBoxDart.isChecked() && sZyouken.contains("芝")) {

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
                            }

                            if (checkBoxShiba.isChecked() && sZyouken.contains("ダ")) {

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
                            }
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
            CheckBox checkBoxShiba = binding.checkboxShiba;
            CheckBox checkBoxDart = binding.checkboxDart;

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


                        requireActivity().runOnUiThread(() -> {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
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

                            if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {

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
                            }

                            if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {

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
                            }
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
                CheckBox checkBoxShiba = binding.checkboxShiba;
                CheckBox checkBoxDart = binding.checkboxDart;

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
                        requireActivity().runOnUiThread(() -> {
                            dateTextLayout.setText(kaisaijo + "   " + kaisaibi);
                            dateTextLayout.setPadding(16, 8, 16, 8);
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

                            if (checkBoxShiba.isChecked() && sZyouken.contains("芝")) {
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
                            }

                            if (checkBoxDart.isChecked() && sZyouken.contains("ダ")) {
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
                            }
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
        requireActivity().runOnUiThread(() -> {
            String[] items = joNameList.toArray(new String[joNameList.size()]);
            ;
            // AlertDialog を作成
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose an Option")
                    .setItems(items, (dialog, which) -> {
                        // ユーザーが選択したアイテムを取得
                        String joName = items[which];
                        joNameList.add(0, joName);
                        TextView textView = binding.selectRaceText;
                        textView.setText(joNameList.get(0) + "競馬場の傾向を表示");
                        joNameList.remove(which + 1);
                    });
            // ダイアログを表示
            builder.create().show();
        });
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