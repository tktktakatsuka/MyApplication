package com.tktkcompany.kakoRaceKeiba.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentDashboardBinding;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ProgressBar progressBar;
    private LinearLayout buttonContainer;

    // 競馬場ごとの開催日とメインレース名（並び替えのたびに再取得しないよう保持する）
    private final Map<String, List<String>> raceDatesByCourse = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> mainRacesByCourse = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        buttonContainer = binding.buttonContainer;

        binding.checkboxDate.setOnCheckedChangeListener((buttonView, isChecked) -> renderAll());

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getJoNames().observe(getViewLifecycleOwner(), joNames -> {
            if (joNames == null || joNames.isEmpty()) return;
            List<String> datelist = WeekendDays.getPastWeekendsInCurrentMonth();
            executeQueriesForAllLocations(datelist, joNames);
        });

        loadBannerAd();
        return root;
    }

    private Bundle setKeyjoNameString(String date, String jo) {
        Bundle bundle = new Bundle();
        bundle.putString("key", date);
        bundle.putString("jo", jo);
        return bundle;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void executeQueriesForAllLocations(List<String> datelist, List<String> joNames) {
        raceDatesByCourse.clear();
        mainRacesByCourse.clear();
        // 取得の完了順に関係なく、競馬場の並びを一定にする
        for (String joName : joNames) raceDatesByCourse.put(joName, null);
        for (String joName : joNames) {
            querySupabaseData(datelist, joName);
        }
    }

    private void querySupabaseData(List<String> dateList, String joName) {
        SupabaseManager.fetchRaceDates(joName, new SupabaseManager.SupabaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> raceDates) {
                if (binding == null || !isAdded()) return;
                List<String> dates = new ArrayList<>();
                for (String date : raceDates) {
                    if (dateList.contains(date)) dates.add(date);
                }
                raceDatesByCourse.put(joName, dates);
                progressBar.setVisibility(View.GONE);
                renderAll();
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("DashboardFragment", "Supabase load failed: " + message);
                if (isAdded()) progressBar.setVisibility(View.GONE);
            }
        });

        SupabaseManager.fetchMainRaceTitles(joName, new SupabaseManager.SupabaseCallback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> titles) {
                if (binding == null || !isAdded()) return;
                mainRacesByCourse.put(joName, titles);
                renderAll();
            }

            @Override
            public void onFailure(@NonNull String message) {
                // メインレース名は補足情報なので、取れなくても日付だけで一覧を出す
                Log.w("DashboardFragment", "Main race titles load failed: " + message);
            }
        });
    }

    private void renderAll() {
        if (binding == null || !isAdded()) return;
        buttonContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (Map.Entry<String, List<String>> entry : raceDatesByCourse.entrySet()) {
            String joName = entry.getKey();
            List<String> dates = entry.getValue();
            if (dates == null || dates.isEmpty()) continue;

            TextView header = new TextView(requireContext());
            header.setText(joName);
            header.setTextSize(18);
            header.setTextColor(0xFF333333);
            header.setTypeface(header.getTypeface(), android.graphics.Typeface.BOLD);
            header.setPadding(0, 32, 0, 8);
            buttonContainer.addView(header);

            List<String> sortedDates = new ArrayList<>(dates);
            if (binding.checkboxDate.isChecked()) {
                Collections.sort(sortedDates, Collections.reverseOrder());
            } else {
                Collections.sort(sortedDates);
            }

            Map<String, String> titles = mainRacesByCourse.get(joName);
            for (String date : sortedDates) {
                String mainRace = titles != null ? titles.get(date) : null;
                buttonContainer.addView(createRaceDayItem(inflater, joName, date, mainRace));
            }
        }
    }

    private View createRaceDayItem(LayoutInflater inflater, String joName, String date, String mainRace) {
        View item = inflater.inflate(R.layout.item_race_day, buttonContainer, false);
        ((TextView) item.findViewById(R.id.text_race_day_date)).setText(WeekendDays.formatShort(date));
        ((TextView) item.findViewById(R.id.text_race_day_main)).setText(mainRace != null ? "11R " + mainRace : "");
        item.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = setKeyjoNameString(date, joName);
            navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
        });
        return item;
    }

    public void loadBannerAd() {
        AdView adView = binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
