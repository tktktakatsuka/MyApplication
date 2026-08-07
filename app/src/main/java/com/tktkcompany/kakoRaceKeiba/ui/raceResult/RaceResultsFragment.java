package com.tktkcompany.kakoRaceKeiba.ui.raceResult;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentRaceresultsBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class RaceResultsFragment extends Fragment {
    private FragmentRaceresultsBinding binding;
    private final List<TableLayout> tableLayouts = new ArrayList<>();
    private final List<TextView> titleTextViews = new ArrayList<>();
    private final List<TextView> timeTextViews = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRaceresultsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews();
        loadBannerAd();

        Bundle args = getArguments();
        if (args != null) {
            String date = args.getString("key");
            String jo = args.getString("jo");
            if (date != null && jo != null) {
                loadRaceResults(date, jo);
            }
        }

        return root;
    }

    private void initViews() {
        for (int i = 1; i <= 12; i++) {
            try {
                // TableLayout
                java.lang.reflect.Field tableField = binding.getClass().getDeclaredField("tableLayout" + i);
                tableLayouts.add((TableLayout) tableField.get(binding));
                // Title TextView (textDashboard1, 2...)
                java.lang.reflect.Field titleField = binding.getClass().getDeclaredField("textDashboard" + i);
                titleTextViews.add((TextView) titleField.get(binding));
                // HassouTime TextView (textHassouTime1, 2...)
                java.lang.reflect.Field timeField = binding.getClass().getDeclaredField("textHassouTime" + i);
                timeTextViews.add((TextView) timeField.get(binding));
            } catch (Exception e) {
                Log.e("RaceResultsFragment", "Error mapping views: " + e.getMessage());
            }
        }
    }

    private void loadRaceResults(String date, String joName) {
        FirebaseManager.queryDataEqualTo("raceResult/" + joName, "kaisaibi", date, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                // レースごとにデータを整理
                for (DataSnapshot raceSnap : snapshot.getChildren()) {
                    String raceNoStr = raceSnap.child("raceNo").getValue(String.class);
                    if (raceNoStr == null) continue;
                    
                    try {
                        int raceNo = Integer.parseInt(raceNoStr);
                        if (raceNo >= 1 && raceNo <= 12) {
                            updateRaceUI(raceNo, raceSnap);
                        }
                    } catch (NumberFormatException e) {
                        Log.e("RaceResultsFragment", "Invalid raceNo: " + raceNoStr);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        });
    }

    private void updateRaceUI(int raceNo, DataSnapshot raceSnap) {
        int index = raceNo - 1;
        TableLayout table = tableLayouts.get(index);
        TextView titleTv = titleTextViews.get(index);
        TextView timeTv = timeTextViews.get(index);

        // ヘッダーが未追加なら追加
        if (table.getChildCount() == 0) {
            titleTv.setText(raceNo + "R   " + raceSnap.child("raceTitle").getValue(String.class));
            titleTv.setPadding(16, 8, 16, 8);

            String hassouTime = raceSnap.child("hassouTime").getValue(String.class);
            timeTv.setText(formatHassouTime(hassouTime));
            timeTv.setTextSize(12);
            timeTv.setPadding(16, 8, 16, 16);

            table.addView(createHeaderRow());
        }

        table.addView(createDataRow(raceSnap));
    }

    private String formatHassouTime(String rawTime) {
        if (rawTime == null) return "";
        
        int ageIndex = rawTime.indexOf("馬齢");
        if (ageIndex != -1) {
            return rawTime.substring(ageIndex);
        }
        
        int startIndex = rawTime.indexOf("発走");
        if (startIndex != -1) {
            return rawTime.substring(0, startIndex + 2);
        }
        
        return rawTime;
    }

    private TableRow createHeaderRow() {
        TableRow row = new TableRow(getContext());
        row.setBackgroundColor(Color.LTGRAY);
        String[] headers = {"着", "枠", "馬名", "性齢", "騎手", "人気", "単勝", "タイム"};
        for (String h : headers) row.addView(createTextView(h));
        return row;
    }

    private TableRow createDataRow(DataSnapshot snap) {
        TableRow row = new TableRow(getContext());
        String[] keys = {"tyaku", "waku", "horseName", "age", "jockey", "popular", "winOdds", "time"};
        for (String key : keys) {
            row.addView(createTextView(snap.child(key).getValue(String.class)));
        }
        return row;
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

    private void loadBannerAd() {
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