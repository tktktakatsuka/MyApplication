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
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentNotificationsBinding;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
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
    private int refreshGeneration;

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

        // 絞り込み条件を変えたらすぐ表に反映する
        binding.checkboxExample.setOnCheckedChangeListener((buttonView, isChecked) -> refreshData());
        binding.checkboxShiba.setOnCheckedChangeListener((buttonView, isChecked) -> refreshData());
        binding.checkboxDart.setOnCheckedChangeListener((buttonView, isChecked) -> refreshData());
        binding.checkboxHani.setOnCheckedChangeListener((buttonView, isChecked) -> refreshData());
    }

    private void setupViewModel() {
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getJoNames().observe(getViewLifecycleOwner(), joNames -> {
            if (joNames == null || joNames.isEmpty()) {
                binding.selectRaceText.setText("現在、開催中の競馬場はありません");
                return;
            }
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
        if (binding == null || joNameList.isEmpty()) return;
        // 取得は非同期なので、後から届いた古い条件の結果を表に混ぜないよう世代で見分ける
        final int generation = ++refreshGeneration;

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
            loadTrendsForDate(generation, date, joNameList.get(0), tableLayouts.get(i), dateTextViews.get(i), category);
        }
    }

    private void clearTables() {
        for (TableLayout tl : tableLayouts) tl.removeAllViews();
        for (TextView tv : dateTextViews) tv.setText("");
    }

    private void loadTrendsForDate(int generation, String date, String joName, TableLayout table, TextView dateText, TrendCategory category) {
        // 全てのRを一括で取得するのは難しい(スキーマ依存)ため、元のロジックを尊重しつつ1R〜12Rをループ
        // ただし、クエリは一回にまとめられるならまとめたい。元のコードは1RごとにqueryDataを呼んでいた。
        // ここでは効率化のため、一回のクエリでその日の全レースを取得し、メモリ上でフィルタリングする。
        
        progressBar.setVisibility(View.VISIBLE);
        
        SupabaseManager.fetchRaceTrends(date, joName, new SupabaseManager.SupabaseCallback<List<SupabaseManager.RaceTrend>>() {
            @Override
            public void onSuccess(List<SupabaseManager.RaceTrend> trends) {
                if (!isAdded() || binding == null || generation != refreshGeneration) return;

                // その日に開催がなければ日付見出しも出さない
                if (trends.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                boolean isHeaderSet = false;
                dateText.setText(joName + "   " + WeekendDays.formatWithDayOfWeek(date));
                dateText.setPadding(16, 8, 16, 8);

                // レース番号でソート (1R, 2R... 12R)
                List<SupabaseManager.RaceTrend> sortedRaces = new ArrayList<>(trends);
                sortedRaces.sort((o1, o2) -> Integer.compare(raceNumber(o1), raceNumber(o2)));

                for (SupabaseManager.RaceTrend trend : sortedRaces) {
                    if (!isHeaderSet) {
                        table.addView(createHeaderRow(category));
                        isHeaderSet = true;
                    }
                    addRaceRow(table, trend, category);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("NotificationsFragment", "Supabase load failed: " + message);
                if (isAdded() && generation == refreshGeneration) progressBar.setVisibility(View.GONE);
            }
        });
    }

    // 「11R」のようなレース番号を数値にする（読み取れない場合は末尾に並べる）
    private static int raceNumber(SupabaseManager.RaceTrend trend) {
        String raceNum = trend.getRaceNum();
        if (raceNum == null) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(raceNum.replace("R", "").trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private TableRow createHeaderRow(TrendCategory category) {
        TableRow row = new TableRow(getContext());
        row.setBackgroundColor(Color.LTGRAY);
        String[] headers;
        if (category == TrendCategory.HARAIMODOSI) {
            headers = new String[]{"レース", "条件", "馬場・天候", "荒れ", "単勝", "馬連", "３連単"};
        } else {
            headers = new String[]{"レース", "条件", "馬場・天候", "１着(人気)", "２着(人気)", "３着(人気)"};
        }
        for (String h : headers) row.addView(createTextView(h));
        return row;
    }

    private void addRaceRow(TableLayout table, SupabaseManager.RaceTrend trend, TrendCategory category) {
        String distance = trend.getRaceDistance();
        if (distance == null) return;

        boolean isShiba = binding.checkboxShiba.isChecked() && distance.contains("芝");
        boolean isDart = binding.checkboxDart.isChecked() && distance.contains("ダ");

        if (!isShiba && !isDart) return;

        TableRow row = new TableRow(getContext());
        // 1行おきに背景色を変えて、横に長い表でも行を追いやすくする
        if (table.getChildCount() % 2 == 0) {
            row.setBackgroundColor(Color.parseColor("#F1F8E9"));
        }
        // レース番号とレース名を1列にまとめて列数を減らす
        row.addView(createTextView(joinNonEmpty(trend.getRaceNum(), trend.getRaceName())));
        row.addView(createTextView(distance));
        row.addView(createTextView(trend.getRaceCondition()));

        if (category == TrendCategory.HARAIMODOSI) {
            row.addView(createTextView(trend.getHaraimodosiAre()));
            row.addView(createTextView(trend.getHaraimodosiTansyo()));
            row.addView(createTextView(trend.getHaraimodosiUmaren()));
            row.addView(createTextView(trend.getHaraimodosi3Rentan()));
        } else {
            String v1 = trend.getUma1Name(), v2 = trend.getUma2Name(), v3 = trend.getUmaName(); // Default HORSE
            switch (category) {
                case KYAKUSITU: v1 = trend.getUma1Kyakusitu(); v2 = trend.getUma2Kyakusitu(); v3 = trend.getUma3Kyakusitu(); break;
                case JOCKEY: v1 = trend.getUma1Jockey(); v2 = trend.getUma2Jockey(); v3 = trend.getUma3Jockey(); break;
                case TYOKYOSI: v1 = trend.getUma1tyokyosi(); v2 = trend.getUma2tyokyosi(); v3 = trend.getUma3tyokyosi(); break;
                case FARTHER: v1 = trend.getUma1father(); v2 = trend.getUma2father(); v3 = trend.getUma3father(); break;
                case MATHER: v1 = trend.getUma1mather(); v2 = trend.getUma2mather(); v3 = trend.getUma3mather(); break;
                case BANUSI: v1 = trend.getBanusi1(); v2 = trend.getBanusi2(); v3 = trend.getBanusi3(); break;
                case SEISAN: v1 = trend.getSeisann1(); v2 = trend.getSeisann2(); v3 = trend.getSeisann3(); break;
            }
            // 馬番と内容を1つのセルにまとめる（例: 「3番 ○○(1人)」）
            row.addView(createTextView(joinNonEmpty(withBan(trend.getUma1Ban()), v1)));
            row.addView(createTextView(joinNonEmpty(withBan(trend.getUma2Ban()), v2)));
            row.addView(createTextView(joinNonEmpty(withBan(trend.getUma3Ban()), v3)));
        }
        table.addView(row);
    }

    private static String withBan(String ban) {
        return ban == null || ban.isEmpty() ? null : ban + "番";
    }

    private static String joinNonEmpty(String a, String b) {
        if (a == null || a.isEmpty()) return b != null ? b : "";
        if (b == null || b.isEmpty()) return a;
        return a + " " + b;
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
        if (joNameList != null && !joNameList.isEmpty()) {
            binding.selectRaceText.setText(joNameList.get(0) + "競馬場の傾向を表示");
        } else {
            binding.selectRaceText.setText("競馬場が選択されていません");
        }
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