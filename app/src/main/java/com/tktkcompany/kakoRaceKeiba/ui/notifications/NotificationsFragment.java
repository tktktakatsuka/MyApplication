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
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.List;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    private List<TableLayout> tableLayouts = new ArrayList<>();
    private List<TextView> dateTextViews = new ArrayList<>();
    private List<String> joNameList = new ArrayList<>();
    private List<String> dateList = new ArrayList<>();
    private ProgressBar progressBar;

    private enum TrendCategory {
        HORSE, KYAKUSITU, JOCKEY, TYOKYOSI, FARTHER, MATHER, HARAIMODOSI, BANUSI, SEISAN
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews();
        setupViewModel();
        setupAd();

        return root;
    }

    private void initViews() {
        progressBar = binding.progressBar;

        // ViewBindingを使って動的にリスト化
        for (int i = 1; i <= 20; i++) {
            try {
                // TableLayout
                java.lang.reflect.Field tableField = binding.getClass().getDeclaredField("tableLayout" + i);
                tableLayouts.add((TableLayout) tableField.get(binding));
                // TextView
                java.lang.reflect.Field textField = binding.getClass().getDeclaredField("textDate" + i);
                dateTextViews.add((TextView) textField.get(binding));
            } catch (Exception e) {
                Log.e("NotificationsFragment", "Error mapping views: " + e.getMessage());
            }
        }

        binding.showDialogButton.setOnClickListener(v -> showRaceCourseDialog());
    }

    private void setupViewModel() {
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getJoNames().observe(getViewLifecycleOwner(), joNames -> {
            if (joNames == null || joNames.isEmpty()) return;
            joNameList = new ArrayList<>(joNames);
            updateRaceText();
            refreshData();
            setupTabs();
        });
    }

    private void setupTabs() {
        List<String> tabTitles = List.of("好走馬", "脚質", "騎手", "調教師", "種牡馬", "母父", "払戻金", "馬主", "生産者");
        TabPagerAdapter adapter = new TabPagerAdapter(requireActivity(), tabTitles);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(tabTitles.get(position))).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refreshData();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void refreshData() {
        if (joNameList.isEmpty()) return;

        // 日付リストの取得とソート
        dateList = WeekendDays.getPastWeekendsInCurrentMonth();
        if (binding.checkboxExample.isChecked()) {
            dateList.sort(Collections.reverseOrder());
        } else {
            Collections.sort(dateList);
        }

        clearTables();
        
        int tabPosition = binding.tabLayout.getSelectedTabPosition();
        TrendCategory category = TrendCategory.values()[Math.max(0, tabPosition)];
        
        int displayLimit = binding.checkboxHani.isChecked() ? dateList.size() : Math.min(dateList.size(), 8);
        
        for (int i = 0; i < Math.min(displayLimit, tableLayouts.size()); i++) {
            String date = dateList.get(i);
            loadTrendsForDate(date, joNameList.get(0), tableLayouts.get(i), dateTextViews.get(i), category);
        }
    }

    private void clearTables() {
        for (TableLayout tl : tableLayouts) tl.removeAllViews();
        for (TextView tv : dateTextViews) tv.setText("");
    }

    private void loadTrendsForDate(String date, String joName, TableLayout table, TextView dateText, TrendCategory category) {
        // 全てのRを一括で取得するのは難しい(スキーマ依存)ため、元のロジックを尊重しつつ1R〜12Rをループ
        // ただし、クエリは一回にまとめられるならまとめたい。元のコードは1RごとにqueryDataを呼んでいた。
        // ここでは効率化のため、一回のクエリでその日の全レースを取得し、メモリ上でフィルタリングする。
        
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseManager.queryDataEqualTo("raceTrends/" + joName, "kaisaibi", date, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                
                boolean isHeaderSet = false;
                dateText.setText(joName + "   " + date);
                dateText.setPadding(16, 8, 16, 8);

                // レース番号順に表示するために一旦リスト化してソートするのもありだが、
                // 元のコードは1R...12Rのループでメソッドを呼んでいた。
                // スナップショットを走査して表示する。
                
                List<DataSnapshot> sortedRaces = new ArrayList<>();
                for (DataSnapshot raceSnap : snapshot.getChildren()) {
                    sortedRaces.add(raceSnap);
                }
                // レース番号でソート (1R, 2R... 12R)
                sortedRaces.sort((o1, o2) -> {
                    String r1 = o1.child("raceNum").getValue(String.class);
                    String r2 = o2.child("raceNum").getValue(String.class);
                    if (r1 == null || r2 == null) return 0;
                    int n1 = Integer.parseInt(r1.replace("R", ""));
                    int n2 = Integer.parseInt(r2.replace("R", ""));
                    return Integer.compare(n1, n2);
                });

                for (DataSnapshot raceSnap : sortedRaces) {
                    if (!isHeaderSet) {
                        table.addView(createHeaderRow(category));
                        isHeaderSet = true;
                    }
                    addRaceRow(table, raceSnap, category);
                }
                
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", error.getMessage());
                if (isAdded()) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private TableRow createHeaderRow(TrendCategory category) {
        TableRow row = new TableRow(getContext());
        row.setBackgroundColor(Color.LTGRAY);
        String[] headers;
        if (category == TrendCategory.HARAIMODOSI) {
            headers = new String[]{"R", "レース名", "条件", "馬場・天候", "荒れ", "単勝", "馬連", "３連単"};
        } else {
            headers = new String[]{"R", "レース名", "条件", "馬場・天候", "馬番", "１着(人気)", "馬番", "２着(人気)", "馬番", "３着(人気)"};
        }
        for (String h : headers) row.addView(createTextView(h));
        return row;
    }

    private void addRaceRow(TableLayout table, DataSnapshot snap, TrendCategory category) {
        String distance = snap.child("raceDistance").getValue(String.class);
        if (distance == null) return;

        boolean isShiba = binding.checkboxShiba.isChecked() && distance.contains("芝");
        boolean isDart = binding.checkboxDart.isChecked() && distance.contains("ダ");

        if (!isShiba && !isDart) return;

        TableRow row = new TableRow(getContext());
        row.addView(createTextView(snap.child("raceNum").getValue(String.class)));
        row.addView(createTextView(snap.child("raceName").getValue(String.class)));
        row.addView(createTextView(distance));
        row.addView(createTextView(snap.child("raceCondition").getValue(String.class)));

        if (category == TrendCategory.HARAIMODOSI) {
            row.addView(createTextView(snap.child("haraimodosiAre").getValue(String.class)));
            row.addView(createTextView(snap.child("haraimodosiTansyo").getValue(String.class)));
            row.addView(createTextView(snap.child("haraimodosiUmaren").getValue(String.class)));
            row.addView(createTextView(snap.child("haraimodosi3Rentan").getValue(String.class)));
        } else {
            String k1 = "uma1Name", k2 = "uma2Name", k3 = "umaName"; // Default HORSE
            switch (category) {
                case KYAKUSITU: k1 = "uma1Kyakusitu"; k2 = "uma2Kyakusitu"; k3 = "uma3Kyakusitu"; break;
                case JOCKEY: k1 = "uma1Jockey"; k2 = "uma2Jockey"; k3 = "uma3Jockey"; break;
                case TYOKYOSI: k1 = "uma1tyokyosi"; k2 = "uma2tyokyosi"; k3 = "uma3tyokyosi"; break;
                case FARTHER: k1 = "uma1father"; k2 = "uma2father"; k3 = "uma3father"; break;
                case MATHER: k1 = "uma1mather"; k2 = "uma2mather"; k3 = "uma3mather"; break;
                case BANUSI: k1 = "banusi1"; k2 = "banusi2"; k3 = "banusi3"; break;
                case SEISAN: k1 = "seisann1"; k2 = "seisann2"; k3 = "seisann3"; break;
            }
            row.addView(createTextView(snap.child("uma1Ban").getValue(String.class)));
            row.addView(createTextView(snap.child(k1).getValue(String.class)));
            row.addView(createTextView(snap.child("uma2Ban").getValue(String.class)));
            row.addView(createTextView(snap.child(k2).getValue(String.class)));
            row.addView(createTextView(snap.child("uma3Ban").getValue(String.class)));
            row.addView(createTextView(snap.child(k3).getValue(String.class)));
        }
        table.addView(row);
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text != null ? text : "");
        tv.setPadding(16, 8, 16, 8);
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(2, Color.BLACK);
        tv.setBackground(gd);
        return tv;
    }

    private void showRaceCourseDialog() {
        if (joNameList.isEmpty()) return;
        String[] items = joNameList.toArray(new String[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle("競馬場を選択する")
                .setItems(items, (dialog, which) -> {
                    String selected = joNameList.remove(which);
                    joNameList.add(0, selected);
                    updateRaceText();
                    refreshData();
                })
                .show();
    }

    private void updateRaceText() {
        binding.selectRaceText.setText(joNameList.get(0) + "競馬場の傾向を表示");
    }

    private void setupAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.setAdListener(new AdListener() {
            @Override public void onAdFailedToLoad(@NonNull LoadAdError adError) { Log.e("Ads", adError.getMessage()); }
        });
        binding.adView.loadAd(adRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}